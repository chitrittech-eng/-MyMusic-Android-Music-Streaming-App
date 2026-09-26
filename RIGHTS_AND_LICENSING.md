# MyMusic — Rights & Licensing Tracking

Implementation reference for the rights confirmation and audit system.  
Everything here runs on the **existing Firebase stack** — no new libraries required.

---

## Table of Contents
- [Overview](#overview)
- [Firestore Schema](#firestore-schema)
- [Kotlin Types](#kotlin-types)
- [Upload Gate Logic](#upload-gate-logic)
- [Firestore Security Rules](#firestore-security-rules)
- [Moderator Review Guide](#moderator-review-guide)
- [Content Seeding Checklist](#content-seeding-checklist)
- [Play Store Submission Checklist](#play-store-submission-checklist)

---

## Overview

The rights system enforces three guarantees before any track is published:

1. **Artist gate (UI)** — The Upload button is disabled until the rights checkbox is ticked.
2. **ViewModel gate (Kotlin)** — `uploadSong()` returns an error immediately if `rightsCheckboxAccepted == false`, before any network call is made. Cannot be bypassed by a modified UI.
3. **Firestore rule gate (server)** — The server rejects any `songs` document where `rightsConfirmed != true`. Cannot be bypassed even by a modified APK.

An immutable audit record is written to `agreement_acceptances` after every successful upload, keyed by `userId + songId + agreementVersion`.

---

## Firestore Schema

### `songs` collection — added fields

| Field | Type | Description |
|---|---|---|
| `licenseType` | `string` | One of: `artist_owned`, `creative_commons`, `royalty_free_licensed`, `public_domain` |
| `rightsConfirmed` | `boolean` | `true` only after artist checks the agreement box |
| `rightsConfirmedAt` | `number` (epoch ms) | When the agreement was accepted |
| `agreementVersion` | `string` | e.g. `"v1.0"` — tracks which agreement text was in force |
| `licenseSource` | `string \| null` | Library name for royalty-free tracks (e.g. `"Jamendo"`); `null` for artist-owned |
| `approvalStatus` | `string` | `"pending"` → `"approved"` \| `"rejected"`. Replaces legacy `isApproved` boolean |

> [!NOTE]
> The legacy `isApproved` boolean is kept for backward compatibility with existing Firestore documents. `approvalStatus` is the canonical field going forward. `approveSong()` and `rejectSong()` write both fields.

### `artists` collection — added field

| Field | Type | Description |
|---|---|---|
| `sourceType` | `string` | `"independent"` or `"licensed_partner"` — future-proofs aggregator catalog support |

### `agreement_acceptances` collection (immutable audit log)

```
agreement_acceptances/{acceptanceId}
  userId:             string   — UID of the artist who accepted
  songId:             string   — Firestore ID of the uploaded song
  agreementVersion:   string   — e.g. "v1.0"
  acceptedAt:         number   — epoch milliseconds
  agreementTextHash:  string   — SHA-256 hex of the agreement text shown on screen
```

> [!IMPORTANT]
> This collection is **write-once, never-update, never-delete** — enforced by Firestore rules. Even admins cannot modify records. This gives you a clean paper trail for disputes.

---

## Kotlin Types

### `LicenseType` enum

```kotlin
// com.mymusic.app.domain.model.LicenseType
enum class LicenseType(val value: String) {
    ARTIST_OWNED("artist_owned"),
    CREATIVE_COMMONS("creative_commons"),
    ROYALTY_FREE_LICENSED("royalty_free_licensed"),
    PUBLIC_DOMAIN("public_domain");

    companion object {
        fun fromValue(value: String): LicenseType =
            entries.firstOrNull { it.value == value } ?: ARTIST_OWNED
    }
}
```

### `Song` — new fields (excerpt)

```kotlin
data class Song(
    // ...existing fields...
    val licenseType: String = LicenseType.ARTIST_OWNED.value,
    val rightsConfirmed: Boolean = false,
    val rightsConfirmedAt: Long? = null,   // epoch ms
    val agreementVersion: String = "",
    val licenseSource: String? = null,
    val approvalStatus: String = "pending" // "pending" | "approved" | "rejected"
)
```

### `AgreementAcceptance`

```kotlin
data class AgreementAcceptance(
    val id: String = "",
    val userId: String = "",
    val songId: String = "",
    val agreementVersion: String = "",
    val acceptedAt: Long = 0L,
    val agreementTextHash: String = ""
)
```

---

## Upload Gate Logic

**File:** [`UploadViewModel.kt`](app/src/main/java/com/mymusic/app/feature/upload/UploadViewModel.kt)

The gate is implemented as a hard return in `uploadSong()` before any I/O:

```kotlin
fun uploadSong() {
    if (!state.rightsCheckboxAccepted) {
        _uiState.update { it.copy(error = "You must confirm you have the rights to upload this track.") }
        return  // No Storage or Firestore calls made
    }
    // ...rest of upload flow
}
```

The upload flow writes rights fields atomically on song doc creation — the doc is **never created without them**:

```kotlin
val song = Song(
    ...
    licenseType       = state.licenseType.value,
    rightsConfirmed   = true,
    rightsConfirmedAt = System.currentTimeMillis(),
    agreementVersion  = Constants.CURRENT_AGREEMENT_VERSION, // "v1.0"
    licenseSource     = licenseSource,
    approvalStatus    = "pending"
)
```

After the song doc and audio upload succeed, `AgreementRepository.recordAcceptance()` is called to write the audit record.

### Bumping the agreement version

1. Update the agreement text in `UploadScreen.kt`
2. Change `Constants.CURRENT_AGREEMENT_VERSION` to `"v1.1"` (or next)
3. Compute a new SHA-256 of the text and update `Constants.AGREEMENT_V1_TEXT_HASH`
4. Old records retain their original version — you can reconstruct exactly what text was shown to each user

---

## Firestore Security Rules

See [`FIREBASE_SETUP.md`](./FIREBASE_SETUP.md) — Section 4 for the full rules.

Key guarantees:

| Rule | Enforcement |
|---|---|
| Songs can only be created with `rightsConfirmed == true` | `allow create` checks the field |
| Songs can only be created with a valid `licenseType` | `allow create` uses `in [...]` |
| Songs are created in `"pending"` status | `allow create` checks `approvalStatus == 'pending'` |
| Only moderators/admins can change `approvalStatus` | `allow update` blocks `affectedKeys()` for artists |
| `agreement_acceptances` is immutable | `allow update, delete: if false` |

---

## Moderator Review Guide

The Moderator screen now has two tabs:

**Tab 1 — Reports:** existing content moderation reports (unchanged)

**Tab 2 — Song Approvals:** all songs in `approvalStatus == "pending"` state

Each pending song card shows:
- Song title, artist name, genre
- **License type** — colour-coded (green = low risk, amber = needs attention)
- **License source** — for royalty-free tracks
- **Rights confirmed status** — with the exact date/time and agreement version
- The **Approve button is disabled** if `rightsConfirmed == false`

### What to look for before approving

| Signal | Action |
|---|---|
| Well-known commercial track uploaded as `artist_owned` by an unverified account | Reject + investigate |
| `licenseSource` is blank for a `royalty_free_licensed` track | Request clarification before approving |
| `rightsConfirmed == false` | Never approve — this should not appear in the queue under normal operation |
| Genre mismatch or suspicious metadata | Use Reject to send back; add a note via the Report system |

---

## Content Seeding Checklist

- [ ] Reach out to 30–50 independent artists directly (local scene, Bandcamp, SoundCloud) — have them upload through the real flow so `rightsConfirmed` data populates naturally
- [ ] Pull tracks from **Jamendo** or **Free Music Archive** — set `licenseType: "creative_commons"` and populate `licenseSource` accordingly
- [ ] Do **not** seed categories implying commercial content you don't have (e.g. no "Top Bollywood Hits" until you have licensed content there)
- [ ] Verify every seeded track has `rightsConfirmed: true` before changing `approvalStatus` to `"approved"` — use the moderator panel

---

## Play Store Submission Checklist

- [ ] **Privacy Policy** — URL live and linked in Play Console (cover Firebase Auth, Firestore, Storage, Analytics data)
- [ ] **Content rating** — flag "user-generated content" since artists upload directly
- [ ] **Data safety form** — reflect what's actually collected: Firebase Auth, Firestore, Storage, Analytics
- [ ] **Terms of Service** — accessible in-app (Settings screen) and via a public URL
- [ ] **Copyright/takedown contact** — dedicated email set up and monitored before launch; you must be able to respond to DMCA complaints quickly once live
- [ ] **Agreement version history** — keep a public changelog of agreement versions (e.g. `/legal/agreement-v1.0.txt`) so the `agreementTextHash` field in audit records is verifiable

---

*Last updated: September 2026 · MyMusic v1.0 · Agreement v1.0*
