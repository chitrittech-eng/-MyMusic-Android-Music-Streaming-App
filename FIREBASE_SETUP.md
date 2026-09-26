# Firebase Setup Instructions

## 1. Create a Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project" → name it "MyMusic"
3. Enable Google Analytics (optional but recommended)

## 2. Add Android App
1. In the Firebase console, click "Add app" → Android
2. Package name: `com.mymusic.app`
3. App nickname: MyMusic
4. Download `google-services.json`
5. Place it at: `app/google-services.json`

## 3. Enable Firebase Services
In the Firebase console, enable:
- **Authentication** → Sign-in methods → Email/Password ✅ and Google ✅
- **Firestore Database** → Create in production mode
- **Storage** → Create default bucket

## 4. Firestore Security Rules
In Firestore → Rules, paste:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can read any user, write only their own
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }

    // Admins can update any user role
    match /users/{userId} {
      allow update: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Songs — Rights gate enforced server-side
    match /songs/{songId} {
      // Read: approved songs are public; artist can always see their own
      allow read: if resource.data.approvalStatus == 'approved'
                  || resource.data.artistId == request.auth.uid
                  || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['moderator', 'admin'];

      // Create: rightsConfirmed MUST be true and licenseType must be a known value
      allow create: if request.auth != null
                    && request.resource.data.rightsConfirmed == true
                    && request.resource.data.licenseType in
                       ['artist_owned', 'creative_commons', 'royalty_free_licensed', 'public_domain']
                    && request.resource.data.approvalStatus == 'pending'
                    && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['artist', 'admin'];

      // Update: artist may update own song but cannot change approvalStatus
      //         moderators/admins may update anything
      allow update: if request.auth != null
                    && (
                         (resource.data.artistId == request.auth.uid
                          && !('approvalStatus' in request.resource.data.diff(resource.data).affectedKeys()))
                       )
                    || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['moderator', 'admin'];

      allow delete: if request.auth.uid == resource.data.artistId
                    || get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Playlists: owner can manage, others can read public ones
    match /playlists/{playlistId} {
      allow read: if resource.data.isPublic == true || request.auth.uid == resource.data.ownerId;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.ownerId ||
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Albums: public read, artist/admin write
    match /albums/{albumId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['artist', 'admin'];
    }

    // Artists: public read, owner/admin write
    match /artists/{artistId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == artistId ||
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Categories: public read, admin write only
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }

    // Reports: authenticated users can submit, moderators/admins can read/update
    match /reports/{reportId} {
      allow create: if request.auth != null;
      allow read, update: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['moderator', 'admin'];
    }

    // Liked songs subcollection
    match /users/{userId}/liked_songs/{songId} {
      allow read, write: if request.auth.uid == userId;
    }

    // Agreement acceptances — immutable audit log
    match /agreement_acceptances/{acceptanceId} {
      // Users can create their own acceptance records only
      allow create: if request.auth != null
                    && request.resource.data.userId == request.auth.uid;
      // Users can read their own; moderators/admins can read all
      allow read: if request.auth != null &&
                     (resource.data.userId == request.auth.uid ||
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['moderator', 'admin']);
      // Immutable — no updates or deletes ever permitted
      allow update, delete: if false;
    }
  }
}
```

## 5. Firebase Storage Rules
In Storage → Rules:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /music/{songId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 50 * 1024 * 1024; // 50MB max
    }
    match /covers/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.resource.size < 5 * 1024 * 1024; // 5MB max
    }
    match /profiles/{userId}/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

## 6. Make First Admin
After signing up with your main account:
1. Go to Firestore → users → find your user document
2. Manually change `role` field from `"listener"` to `"admin"`

That's it! The app will automatically show the Admin Panel in Settings for admin users.
