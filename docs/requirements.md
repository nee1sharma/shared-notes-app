# SharedNoteBook Requirements

**Document status:** Product baseline with laptop control-plane decisions  
**Last updated:** 2026-08-09  
**Platforms:** Android member app and web companion with web-only administration  

## 1. Product definition

SharedNoteBook consists of local-first Android member applications and a stateful laptop control plane. Family members use Android for private and shared notes. The designated admin laptop runs the Java Spring Boot backend, PostgreSQL, discovery/registry service, and web companion. Administrators use the web companion for privileged operations, device oversight, policy management, shared-note deletion, and audit visualization.

Shared data is stored on registered Android devices and as a complete durable replica in PostgreSQL on the admin laptop. Android devices continue to synchronize peer-to-peer when they can reach one another on the same local-area network (LAN). The laptop reconciles its database with peers whenever the backend is available.

Android note creation, reading, editing, and peer synchronization do not require the internet or the admin laptop to be running. Web access, admin operations, live global presence, device-registry updates, and PostgreSQL reconciliation require the laptop backend to be running.

## 2. Terminology

- **Household:** One family notebook and its registered members, devices, shared notes, security keys, policies, and audit activity.
- **Home LAN:** The LAN on which the household is created and on which new devices may request or complete registration according to the household enrollment policy. This is normally the family's home Wi-Fi.
- **Member:** A person registered in the household. A member may use multiple devices.
- **Device:** One Android app installation with its own cryptographic identity.
- **Registered device:** A device that joined the household from the home LAN. Registration replaces the earlier concept of an admin-approved device.
- **Pending device:** A device that submitted a registration request while approval-required enrollment was enabled but has not been approved or rejected.
- **Admin:** A member permitted to manage devices, policies, shared-note deletion, and the administrative activity log.
- **Root admin laptop:** The designated laptop that owns the initial admin identity and runs the stateful household control plane.
- **Discovery service:** A Spring Boot service on the root admin laptop that stores the registered-device directory and aggregates authenticated presence heartbeats.
- **Global presence:** The latest household-wide connected/offline view known to the running discovery service. It is unavailable, not magically maintained, while the laptop backend is stopped.
- **PostgreSQL replica:** The durable laptop database containing all shared notes, shared revisions retained by policy, device/member records, activity, policies, trash, and synchronization state. It never contains private Android notes.
- **Web companion:** The laptop/browser interface for shared notes and the only user interface allowed to display admin data or initiate privileged operations.
- **Private note:** A note encrypted and stored only on its originating device.
- **Shared note:** A note available to and editable by all registered, non-revoked household devices.
- **Revision history:** Retained previous content versions of a note.
- **Activity history:** Admin-only audit metadata describing shared-note, device, synchronization, security, and administrative actions.

## 3. Product principles

1. Private notes never leave their device unless the user explicitly converts them to shared notes.
2. Shared notes are editable by all registered household members.
3. Android devices synchronize directly with peers and do not depend on a permanent Android server; the designated laptop is the persistent control plane and PostgreSQL replica when running.
4. A device can enroll only from the household's home LAN and must follow the admin-configured enrollment policy.
5. After registration, devices may synchronize on any LAN where registered peers meet.
6. Notes and network traffic must be encrypted.
7. Administrative visibility must not expose private-note content or metadata.
8. Offline work must be safe and must not be silently overwritten.
9. The Android application must not display admin pages or initiate admin-only operations, even when its member has the admin role.
10. Registered Android peers must still validate and apply signed admin changes received through synchronization.
11. Stopping the laptop backend must not prevent Android devices from reading, editing, or peer-synchronizing notes.
12. Private Android notes must never be uploaded to PostgreSQL or exposed to the web companion.
13. Admin and discovery availability must be reported honestly; the UI must not claim global live presence when the laptop backend is stopped.

### 3.1 Confirmed control-plane decisions

- The backend runs on the designated admin laptop.
- The backend is stateful and uses PostgreSQL for the complete shared household dataset.
- The backend uses Java Spring Boot.
- The web companion uses React with TypeScript; Spring Boot exposes the authenticated JSON API and serves the production frontend assets.
- The discovery/registry service runs as part of the laptop backend.
- The laptop is the initial root admin.
- The root admin can delegate the admin role to another registered member/mobile identity.
- Delegation does not add admin pages to the native Android app. A delegated mobile admin uses the web companion for privileged actions unless this earlier platform rule is explicitly changed.
- Android work continues when the backend is stopped; web/admin/global-presence features pause and reconcile when it restarts.
- Shared-note deletion uses admin-managed trash.
- Activity retention is an admin configuration.
- Closed-browser system notifications are not required.
- Admin authentication and sensitive-action reauthentication use WebAuthn passkeys. Version 1 has no password fallback.
- Presence on the configured home Wi-Fi is sufficient for `OPEN_ON_HOME_LAN` enrollment; no code or admin approval is added in that mode.

## 4. Roles and permissions

### 4.1 Member

A member can:

- Create, view, edit, and save shared notes.
- Create, view, edit, and delete private notes on their current device.
- Convert a private note to a shared note.
- Copy a shared note into a new private note without removing the shared note.
- View the retained revision history of an accessible note.
- Restore an accessible retained revision as a new current revision.
- View synchronization state for their device.

A member cannot:

- Delete a shared note.
- View the household activity log.
- Approve or reject pending device registrations.
- Change household-wide history settings.
- Block or revoke devices.
- Grant or remove admin privileges.
- Access any admin dashboard or initiate any admin-only operation from Android.

### 4.2 Admin

An admin has all normal member permissions in Android. The admin role adds the following capabilities only after authentication in the web companion:

- View the admin dashboard.
- View registered, connected, blocked, and revoked devices.
- View admin-visible activity history.
- Approve or reject pending device registrations when approval-required enrollment is enabled.
- Move shared notes to trash, restore them, permanently purge them, and coordinate shared-to-private delivery.
- Block, unblock, and revoke registered devices.
- Change revision-history and activity-retention settings.
- Change the home-LAN enrollment policy.
- Grant or remove admin privileges, while ensuring the household always retains at least one admin.

### 4.3 Initial admin

The designated laptop is the root admin node and creates the first admin identity during backend setup. The root admin can grant the admin role to another registered member or mobile identity from the web companion. Administrative role changes must be authenticated, persisted in PostgreSQL, signed, and synchronized to Android peers.

A delegated mobile identity does not unlock native Android admin pages. It authorizes that member to authenticate to the web companion as an admin. Enabling native Android administration would reverse the explicit web-only requirement and requires a separate product decision.

### 4.4 Platform capability boundary

| Capability | Android app | Web companion |
|---|---:|---:|
| Create/edit private notes | Yes | No |
| Create/edit shared notes | Yes | Yes |
| View shared-note revision history | Yes | Yes |
| Copy shared content to a private Android note | Yes | No |
| Delete a shared note | No | Yes |
| Approve/reject registrations | No | Yes |
| Block/revoke devices | No | Yes |
| View activity history | No | Yes |
| Change household policies | No | Yes |
| Manage admin roles | No | Yes |

## 5. Functional requirements

### 5.1 Household creation and device registration

- **FR-001:** The Spring Boot backend on the designated laptop shall create the household and initialize PostgreSQL.
- **FR-002:** Household creation shall generate a unique household identity, root-admin identity, discovery-service identity, and cryptographic material required for secure peer recognition.
- **FR-003:** The designated laptop shall become the root admin node. The first Android device joins as a member unless the root admin delegates the admin role through the web companion.
- **FR-004:** A new device on the home LAN shall discover the laptop discovery service when it is running and begin registration under the current enrollment policy. When the backend is stopped, already registered Android peers may still discover and synchronize with each other, but new control-plane registration is unavailable.
- **FR-005:** Registration shall require the member's name.
- **FR-006:** Registration shall automatically suggest a device name using Android device information.
- **FR-007:** The suggested device name shall be editable before registration is completed.
- **FR-008:** Registration shall accept an optional email address.
- **FR-009:** Each registered app installation shall receive a unique device ID and cryptographic identity.
- **FR-010:** A completed registration shall be persisted in PostgreSQL and create an admin-visible activity event.
- **FR-011:** A registered device shall remain registered when it leaves the home LAN.
- **FR-012:** A registered device shall be allowed to synchronize on any LAN where it can reach another valid registered household device.
- **FR-013:** An unregistered or revoked device shall not receive shared-note data during normal synchronization.
- **FR-014:** A device joining as a new person shall create a new member identity even if its display name matches an existing member.
- **FR-015:** Adding another device to an existing member shall require cryptographic proof from one of that member's registered devices; a matching typed name shall not be proof of identity or admin status.
- **FR-016:** Existing-member device linking shall require an explicit local pairing action by that member, such as scanning a one-time QR code, and shall additionally follow the household enrollment policy.
- **FR-017:** Only the laptop discovery service shall advertise new-device registration, and only while its current LAN matches the stored home-LAN enrollment profile. Registered Android peers may advertise their authenticated synchronization service on any LAN.

The default policy is open home-LAN registration, preserving the zero-approval joining experience. Admins may switch the household to approval-required registration. Existing registered devices are unaffected by this choice.

### 5.2 Notes

- **FR-020:** A member shall be able to create a note with at least a title and text body.
- **FR-021:** A note shall have exactly one current visibility: private or shared.
- **FR-022:** The note list shall allow users to distinguish and filter private and shared notes.
- **FR-023:** A private note shall be encrypted and stored only on the device that owns it.
- **FR-024:** Private-note content, title, access events, and revision metadata shall not be synchronized or included in the household admin activity log.
- **FR-025:** A shared note shall be editable by every registered, non-revoked member device.
- **FR-026:** Each successful shared-note save shall create a uniquely identifiable revision.
- **FR-027:** A user shall be able to convert a private note into a shared note.
- **FR-028:** Private-to-shared conversion shall require confirmation that all registered household devices may receive the note.
- **FR-029:** Any member shall be able to copy a shared note into a private note on the current device without changing or removing the shared note.
- **FR-030:** Copying a shared note to private on Android shall leave the shared note unchanged. The Android app shall not offer “move shared note to private” because removing the shared version is an admin operation.
- **FR-031:** The Android app shall explain that copying to private does not remove or reduce access to the shared source.
- **FR-032:** Only an authenticated admin using the web companion shall be allowed to move a shared note to trash, restore it, or permanently purge it.
- **FR-033:** Trashing shall synchronize a reversible trashed state. Permanent purge shall synchronize a content-free tombstone so an offline peer cannot recreate the note.
- **FR-034:** Permanent purge shall remove active content and retained content revisions from cooperating devices after synchronization.
- **FR-035:** Shared-note create, open, save, conversion, revision restore, conflict, trash, restore, and purge operations shall produce the applicable admin-visible activity events.

Attachments, drawing, audio, video, and rich-text collaboration are outside the version 1 scope. “File access” on the admin page means access to a shared note in version 1.

### 5.3 Revision history

- **FR-040:** Each note shall retain a configurable number of saved content revisions.
- **FR-041:** The household default shall be the latest 5 saved revisions per note.
- **FR-042:** An admin shall be able to change the household revision limit.
- **FR-043:** A reduced limit shall prune excess oldest revisions after the setting is synchronized.
- **FR-044:** Restoring an earlier revision shall create a new current revision; it shall not rewrite or remove the existing history entry.
- **FR-045:** A newly registered device shall receive only the current version of each shared note during initial synchronization.
- **FR-046:** A new device shall not receive content revisions that predate its registration.
- **FR-047:** After registration, the device shall retain revisions it creates or receives, subject to the current household limit.
- **FR-048:** Revision history is note content and is distinct from admin-only activity history.

### 5.4 Peer-to-peer synchronization

- **FR-050:** Registered devices shall discover reachable household peers on a LAN.
- **FR-051:** Devices shall mutually authenticate before exchanging household data.
- **FR-052:** Shared-note data shall travel only over encrypted, authenticated sessions.
- **FR-053:** Synchronization shall not require internet connectivity or a central service.
- **FR-054:** Every peer shall be capable of forwarding valid changes received from another peer, allowing changes to propagate when the original author device is absent.
- **FR-055:** Synchronization shall be automatic when the app is active and a peer becomes reachable.
- **FR-056:** The app shall schedule background reconciliation using Android-supported background work, subject to operating-system restrictions.
- **FR-057:** The app shall attempt synchronization after a shared note is saved and after a relevant network change.
- **FR-058:** The app shall provide a manual “Sync now” action.
- **FR-059:** The UI shall expose whether data is synchronized, pending, conflicting, offline, or failed.
- **FR-060:** Synchronization messages and changes shall be idempotent so retries do not create duplicate notes, revisions, activity events, or device records.
- **FR-061:** A returning registered device shall receive changes it missed while offline.
- **FR-062:** Device and admin-policy changes, revocation records, deletion markers, and eligible audit events shall propagate peer-to-peer.
- **FR-063:** Failure to discover peers because of router client isolation shall be shown as a diagnostic condition rather than interpreted as data loss.
- **FR-064:** Android peers shall discover the laptop discovery service when it is reachable on an allowed LAN and shall reconnect without manual address entry when possible.
- **FR-065:** The Android diagnostics page shall show whether the laptop control plane is connected, unavailable, or reconciling; Android shall not start or host the web companion.
- **FR-066:** Every browser and device request shall be authenticated and authorized by the laptop backend even when enrollment is configured as open on the home LAN.

### 5.5 Concurrent editing and conflicts

- **FR-070:** Version 1 shall use save-based collaboration, not real-time character-by-character co-editing.
- **FR-071:** Each saved revision shall identify the revision from which it was edited.
- **FR-072:** Concurrent saves derived from the same earlier revision shall be detected as a conflict.
- **FR-073:** A conflict shall preserve both versions and shall never silently overwrite either version.
- **FR-074:** An authorized member shall be able to compare the versions and resolve the conflict by choosing or combining content.
- **FR-075:** Conflict resolution shall create a new revision and an admin-visible resolution activity.

### 5.6 Web-only device administration

- **FR-080:** The web companion's admin area shall have a page listing all household devices and their registration state.
- **FR-081:** For each device, the admin page shall show member name, device name, role, registration time, last-seen time, and reachable/offline/blocked/revoked state when known.
- **FR-081A:** Android device identity shall come from authenticated registration and shall include the registered member name, editable device name, application name `SharedNoteBook Android`, manufacturer/model, platform, and stable short identifier.
- **FR-081B:** A device shall be presented as connected only while its latest authenticated presence is inside the configured heartbeat window; otherwise it shall be offline with its last-seen time.
- **FR-081C:** Neither Android nor web builds shall seed or display fabricated household devices, member names, activity, note records, or presence counts.
- **FR-082:** An authenticated web admin shall be able to view recent shared-note access and synchronization activity attributable to a device.
- **FR-083:** Blocking a device shall deny synchronization until it is unblocked.
- **FR-084:** Revoking a device shall permanently remove its eligibility to receive subsequent household updates unless it registers again as a new identity on the home LAN.
- **FR-085:** Revocation shall not attempt to delete or invalidate data already stored by the revoked device.
- **FR-086:** Revocation shall rotate the keys used for future shared-note and admin data so the revoked identity cannot decrypt future updates.
- **FR-087:** Device management and key rotation shall produce admin-visible activity events.

### 5.7 Web-only admin activity history

- **FR-090:** Household peers shall maintain an encrypted activity log, and only the authenticated web companion admin area shall present its decrypted contents.
- **FR-091:** Activity retention shall default to 100 days.
- **FR-092:** Admins shall be able to configure the retention duration.
- **FR-093:** Reducing retention shall purge expired events after the policy is synchronized.
- **FR-094:** Activity events shall have a unique ID, event type, recorded time, originating Android device or web admin client, actor member when applicable, target identifiers when applicable, outcome, and non-sensitive event metadata.
- **FR-095:** Activity events created offline shall propagate through eligible registered peers and become available in PostgreSQL after the laptop backend reconnects and reconciles with a peer holding those events.
- **FR-096:** Android peers may store and forward opaque encrypted admin activity, but only an authenticated web admin client shall receive the key needed to decrypt it.
- **FR-097:** Activity records shall be append-only during their retention period, except for expiration-based purging.
- **FR-098:** The web activity page shall support filtering by date, event category, member, device, shared note, and outcome.
- **FR-099:** A note-open event means that the app displayed the note; it is not proof that a human read or understood the content.
- **FR-100:** A synchronization event shall be distinguishable from a user opening a note.
- **FR-101:** Private-note identifiers, titles, content, and actions shall not appear in the shared admin activity log.
- **FR-102:** Because the system is peer-to-peer, admin activity views shall be eventually consistent and shall indicate their latest synchronization time.

### 5.8 Configurable home-LAN enrollment

- **FR-110:** The household enrollment policy shall provide exactly two version 1 modes: `OPEN_ON_HOME_LAN` and `ADMIN_APPROVAL_REQUIRED`.
- **FR-111:** `OPEN_ON_HOME_LAN` shall be the default for a newly created household.
- **FR-112:** Only an admin shall be allowed to change the enrollment policy.
- **FR-113:** In open mode, a new device on the home LAN shall complete registration immediately after identity creation and shall not wait for an admin action.
- **FR-114:** In approval-required mode, a new device on the home LAN shall submit a signed registration request and enter a pending state.
- **FR-115:** A pending device shall not receive household shared-data keys, shared-note content, admin activity, or normal synchronization access.
- **FR-116:** Pending requests shall propagate through registered peers so an admin can review them when activity synchronizes.
- **FR-117:** An admin shall be able to approve or reject a pending request only from the web admin device-management page.
- **FR-118:** Approval shall register the device and make current-version bootstrap available when that device next reconnects on the home LAN.
- **FR-119:** Rejection shall prevent the request identity from completing registration; the device may submit a new request later unless it has been blocked.
- **FR-120:** If no admin is reachable, a request in approval-required mode shall remain pending and the joining UI shall explain that the app may be closed and checked again later.
- **FR-121:** The web companion shall show pending requests when an admin next opens or refreshes it. Version 1 shall not send closed-browser or operating-system notifications.

### 5.8.1 Web shared-note listing and search

- **FR-180:** The web companion shall make all current shared notes available through a paginated list; private notes and trashed notes shall not appear in this list.
- **FR-181:** The first request shall return at most 20 notes.
- **FR-182:** When more matching notes exist, the page shall show a `Show more` action that loads the next 20 without discarding the existing rows.
- **FR-183:** Search shall provide explicit filters for at least note title, creator, last editor, modified-date range, and conflict state.
- **FR-184:** Search and filtering shall not require a plaintext title/body search index in PostgreSQL. Version 1 may decrypt authorized shared-note summaries in backend memory and return only the requested page.
- **FR-185:** Pagination shall use a stable opaque cursor so concurrent edits do not create obvious duplicates or skips within one browsing session.
- **FR-122:** Enrollment policy changes, requests, approvals, and rejections shall create admin-visible activity events.
- **FR-123:** The signed, versioned enrollment policy shall synchronize peer-to-peer, and each peer shall enforce the latest valid policy version known to it.

### 5.9 Stateful laptop backend and PostgreSQL

- **FR-130:** The household backend shall run on the designated admin laptop as a Spring Boot application.
- **FR-131:** PostgreSQL shall store the complete shared household dataset: current shared notes, retained shared revisions, members, Android and web devices, roles, policies, activity events, trash, presence history, synchronization receipts, conflicts, and deletion markers.
- **FR-132:** PostgreSQL shall never store private Android note IDs, titles, bodies, revisions, search indexes, or activity.
- **FR-133:** Shared note content stored in PostgreSQL shall be encrypted at rest using application-managed envelope encryption in addition to database and disk protections.
- **FR-134:** The backend shall reconcile PostgreSQL with reachable Android peers at startup, after peer discovery, and on the configured schedule.
- **FR-135:** Reconciliation shall be idempotent and conflict-aware; the laptop shall not silently overwrite valid offline Android revisions.
- **FR-136:** Administrative roles, device status, retention policy, enrollment policy, trash policy, and admin-issued operations shall use the laptop control plane as their authoritative source and shall propagate to Android peers.
- **FR-137:** Shared note content shall remain multi-source and eventually consistent so Android editing can continue while the backend is stopped.
- **FR-138:** Stopping the backend shall not block private notes, Android shared-note editing, or Android-to-Android synchronization.
- **FR-139:** While the backend is stopped, the web companion, admin actions, new control-plane enrollment, PostgreSQL writes, and global-presence aggregation shall be unavailable. On restart, queued Android changes shall reconcile without data loss.

### 5.10 Discovery service and global presence

- **FR-140:** The Spring Boot backend shall expose an authenticated household discovery/registry service on the LAN.
- **FR-141:** The discovery service shall persist every registered Android and web device in PostgreSQL.
- **FR-142:** Registered devices shall send authenticated presence heartbeats at the configured interval while able to reach the laptop.
- **FR-143:** The discovery service shall derive `Connected`, `Offline`, `Blocked`, and `Revoked` states using authenticated sessions, heartbeat deadlines, and administrative status.
- **FR-144:** The web admin dashboard shall show the globally aggregated state known to the discovery service, including the observation time and backend-running status.
- **FR-145:** A device that cannot currently reach the laptop shall be shown as `Offline` or `Last seen`, even if it may be synchronizing peer-to-peer elsewhere.
- **FR-146:** Discovery/registry availability shall not be a prerequisite for already registered Android peers to discover one another and synchronize locally.
- **FR-147:** The service shall use mDNS/DNS-SD for LAN advertisement plus an authenticated direct-address fallback.
- **FR-148:** Registration, heartbeat, reconnect, and discovery endpoints shall be rate-limited and mutually authenticated where device identity already exists.

### 5.11 Admin-managed trash

- **FR-150:** Web-admin deletion shall move a shared note into household trash rather than immediately destroy it.
- **FR-151:** Trashed shared notes shall disappear from normal Android and web note lists after synchronization.
- **FR-152:** Trashed content and retained revisions shall remain encrypted in PostgreSQL and cooperating peers until restore or purge.
- **FR-153:** Only an authenticated web admin shall view trash, restore a note, or permanently purge it.
- **FR-154:** Trash retention shall default to 30 days and shall be configurable by admins.
- **FR-155:** Restoring a note shall create a new current revision and synchronize it to peers.
- **FR-156:** Permanent purge or retention expiry shall remove retained content and publish a content-free tombstone so offline devices cannot resurrect the note.
- **FR-157:** Trash, restore, and purge operations shall be idempotent and fully audited.

### 5.12 Coordinated shared-to-private workflow

- **FR-160:** An authenticated web admin shall be able to request that a shared note be moved to a private note on a selected registered Android device belonging to that admin member.
- **FR-161:** The shared note shall remain active until the selected Android device receives the authenticated request, creates and durably encrypts the private copy, and returns an acknowledgement.
- **FR-162:** After acknowledgement, the backend shall move the shared note to admin-managed trash and synchronize that state.
- **FR-163:** If the target device is offline, rejects the request, or cannot save the private copy, the workflow shall remain pending or fail without removing the shared note.
- **FR-164:** The browser and PostgreSQL shall never retain the resulting private-note ID or content beyond the minimum opaque acknowledgement required to complete the workflow.
- **FR-165:** Request, completion, cancellation, timeout, and failure outcomes shall be admin-visible activities.

### 5.13 Configuration management

- **FR-170:** Every deployment setting and every admin-configurable default/bound shall be defined under documented keys in `application.yaml`.
- **FR-171:** Secrets such as database passwords, master keys, and private credentials shall be referenced from environment variables or an external secret source, never written literally in `application.yaml`.
- **FR-172:** Deployment settings such as ports, bind addresses, database connectivity, discovery intervals, and cryptographic provider settings shall be read from `application.yaml` at startup.
- **FR-173:** Household settings changed by an admin shall be persisted in PostgreSQL; `application.yaml` supplies their initial defaults and allowed bounds rather than being rewritten at runtime.
- **FR-174:** The effective configuration and its source (`YAML default`, `environment override`, or `database override`) shall be visible to the root admin without exposing secrets.
- **FR-175:** Invalid or unsafe configuration shall fail startup or reject the admin change with a clear diagnostic; it shall not silently fall back to an insecure value.

## 6. Version 1 activity-event catalog

These are the activity types the application shall create. Private-note-only actions are deliberately excluded.

### 6.1 Household, member, and device events

| Event type | Created when |
|---|---|
| `HOUSEHOLD_CREATED` | The admin laptop backend initializes a household. |
| `BACKEND_STARTED` | The Spring Boot control plane starts successfully. |
| `BACKEND_STOPPED` | The Spring Boot control plane completes a graceful shutdown. |
| `DISCOVERY_SERVICE_STARTED` | The laptop discovery/registry service begins accepting devices. |
| `DISCOVERY_SERVICE_STOPPED` | The discovery/registry service stops gracefully. |
| `DEVICE_PRESENCE_CHANGED` | The discovery service changes a device between connected and offline based on authenticated presence. |
| `DEVICE_REGISTRATION_REQUESTED` | A new device submits a request while approval-required enrollment is enabled. |
| `DEVICE_REGISTRATION_APPROVED` | An admin approves a pending device request. |
| `DEVICE_REGISTRATION_REJECTED` | An admin rejects a pending device request. |
| `DEVICE_REGISTERED` | A device completes registration, immediately in open mode or after approval in approval-required mode. |
| `DEVICE_CONNECTION_STARTED` | An authenticated peer session is established. |
| `DEVICE_CONNECTION_ENDED` | A peer session ends or expires. |
| `DEVICE_RENAMED` | A registered device name is changed. |
| `MEMBER_PROFILE_UPDATED` | A member changes shared registration information. |
| `ADMIN_ROLE_GRANTED` | An admin grants admin privileges to a member. |
| `ADMIN_ROLE_REMOVED` | An admin removes another member's admin privileges. |
| `DEVICE_BLOCKED` | An admin temporarily blocks a device. |
| `DEVICE_UNBLOCKED` | An admin removes a temporary block. |
| `DEVICE_REVOKED` | An admin revokes a device identity. |

### 6.2 Shared-note access and content events

| Event type | Created when |
|---|---|
| `SHARED_NOTE_CREATED` | A new shared note is first saved. |
| `SHARED_NOTE_OPENED` | A shared note is displayed to a member in the editor or viewer. |
| `SHARED_NOTE_SAVED` | An edit creates a new current revision. |
| `PRIVATE_NOTE_SHARED` | A private note is converted into a shared note. No former private metadata is logged beyond the resulting shared-note identity. |
| `SHARED_NOTE_REVISION_RESTORED` | A retained revision is restored as a new revision. |
| `SHARED_NOTE_CONFLICT_DETECTED` | Concurrent revisions are detected. |
| `SHARED_NOTE_CONFLICT_RESOLVED` | A member resolves a conflict and saves the resolution. |
| `SHARED_NOTE_TRASHED` | A web admin moves a shared note into household trash. |
| `SHARED_NOTE_RESTORED` | A web admin restores a trashed note as a new current revision. |
| `SHARED_NOTE_PURGED` | A web admin or retention job permanently removes trashed content and publishes its tombstone. |
| `SHARED_TO_PRIVATE_REQUESTED` | A web admin requests private delivery to an eligible Android device. |
| `SHARED_TO_PRIVATE_COMPLETED` | The target Android device acknowledges durable private creation and the shared note moves to trash. |
| `SHARED_TO_PRIVATE_FAILED` | The target or backend cannot safely complete the workflow. |
| `SHARED_TO_PRIVATE_CANCELLED` | A web admin cancels a pending workflow before completion. |

### 6.3 Synchronization events

| Event type | Created when |
|---|---|
| `SYNC_STARTED` | A device starts a synchronization session with a peer. |
| `SHARED_NOTE_SYNCED` | A current shared-note revision is successfully transferred and accepted. |
| `DELETION_SYNCED` | A shared-note tombstone is successfully transferred and accepted. |
| `SYNC_COMPLETED` | A synchronization session completes, including item counts and duration. |
| `SYNC_FAILED` | A synchronization session or eligible item fails, including a safe error category. |
| `POSTGRES_RECONCILIATION_STARTED` | The backend begins reconciling PostgreSQL with reachable peers. |
| `POSTGRES_RECONCILIATION_COMPLETED` | Reconciliation completes with item and conflict counts. |
| `POSTGRES_RECONCILIATION_FAILED` | Reconciliation fails with a safe error category and retry state. |

### 6.4 Security and policy events

| Event type | Created when |
|---|---|
| `UNAUTHORIZED_CONNECTION_REJECTED` | An unknown, blocked, revoked, or cryptographically invalid peer is rejected. |
| `ADMIN_COMMAND_REJECTED` | A peer rejects a stale, replayed, incorrectly signed, or unauthorized privileged command. |
| `HOUSEHOLD_KEYS_ROTATED` | Future-data keys are rotated, normally after revocation or admin-role changes. |
| `ENROLLMENT_POLICY_CHANGED` | An admin changes home-LAN enrollment between open and approval-required mode. |
| `REVISION_LIMIT_CHANGED` | An admin changes the saved-revision limit. |
| `ACTIVITY_RETENTION_CHANGED` | An admin changes the activity-retention duration. |
| `TRASH_RETENTION_CHANGED` | An admin changes the trash-retention duration. |
| `REVISION_HISTORY_PRUNED` | The system removes note revisions to enforce the configured limit. |
| `ACTIVITY_HISTORY_PRUNED` | The system removes expired administrative events. |

### 6.5 Web companion events

| Event type | Created when |
|---|---|
| `WEB_DEVICE_APPROVAL_REQUESTED` | A new web device enters the admin-approval queue. |
| `WEB_DEVICE_ACCEPTED` | An admin or home-LAN policy accepts a web device. |
| `WEB_DEVICE_RENAMED` | An accepted web device's display name changes. |
| `WEB_DEVICE_BLOCKED` | An admin blocks an accepted web device. |
| `WEB_DEVICE_UNBLOCKED` | An admin unblocks a web device. |
| `WEB_DEVICE_REVOKED` | An admin permanently invalidates a web-device credential. |
| `WEB_SESSION_STARTED` | An accepted web device establishes an authenticated laptop-backend session. |
| `WEB_SESSION_ENDED` | A web session disconnects, expires, is terminated, or fails. |
| `WEB_ACCESS_POLICY_CHANGED` | An admin changes web enrollment, timeout, or synchronization policy. |
| `ADMIN_PASSKEY_REGISTERED` | An admin registers a new passkey, including the initial root-admin passkey. |
| `ADMIN_PASSKEY_REMOVED` | An admin removes or revokes a registered passkey. |
| `ADMIN_PASSKEY_REAUTHENTICATED` | A fresh passkey assertion authorizes a sensitive operation. |

Routine discovery broadcasts are not audit events. Failed passkey assertions are recorded in the laptop-local security log with safe metadata but are not synchronized as household activity in version 1.

## 7. Security requirements

- **SEC-001:** All stored private and shared note content shall be encrypted at rest.
- **SEC-002:** Long-lived device private keys and local key-wrapping keys shall be protected by Android Keystore where supported.
- **SEC-003:** Peer sessions shall provide encryption, integrity protection, replay resistance, and mutual device authentication.
- **SEC-004:** Shared-note keys shall be available only to registered, non-revoked Android devices and the authorized laptop backend.
- **SEC-005:** Admin activity decryption shall be available only through an authenticated web companion session belonging to a current admin.
- **SEC-006:** Private notes shall use device-local encryption keys distinct from household shared-data keys.
- **SEC-007:** Logs, crash reports, notifications, and diagnostics shall not contain note content, encryption keys, registration secrets, or optional email addresses.
- **SEC-008:** The web companion's admin area shall authenticate admins with WebAuthn passkeys and require a fresh passkey assertion before destructive, role-changing, credential-management, or key-management actions when the configured reauthentication window has expired.
- **SEC-009:** A revoked device shall retain access to data it already obtained; the system guarantees only that cooperating peers deny future synchronization and that future encrypted updates use rotated keys.
- **SEC-010:** Peer-delivered records shall be authenticated and validated before they modify local data.
- **SEC-011:** The Android app package shall not contain hidden admin screens, routes, or controls.
- **SEC-012:** Android UI code shall not expose or locally invoke privileged behavior merely because the associated member is an admin. Android may validate and apply authenticated administrative records received from the laptop backend or another peer.
- **SEC-013:** Android peers shall accept admin changes only when the command and resulting household record can be authenticated to a currently authorized web admin session under the selected authorization protocol.
- **SEC-014:** The web companion shall operate in a validated secure browser context and shall not persist household keys, note content, or unwrapped admin secrets in ordinary web storage.
- **SEC-015:** PostgreSQL credentials, the database encryption master key, and root-admin secrets shall be supplied through environment/external secret references and shall never be committed in YAML or source control.
- **SEC-016:** The discovery service shall expose no note content and shall authenticate device registration, heartbeat, and status updates.
- **SEC-017:** The admin HTTP UI shall bind to loopback by default. Any later LAN browser listener shall remain disabled until a trusted HTTPS origin is configured.

## 8. Core data requirements

The exact schema may evolve, but version 1 requires these conceptual records:

- `Household`: identity, home-LAN enrollment identity, creation time, current policy version.
- `Member`: ID, display name, optional email, role, status.
- `Device`: ID, member ID, display name, public identity key, registration time, last seen, status.
- `Note`: ID, current visibility, title, body, creator, current revision ID, timestamps, deletion state.
- `NoteRevision`: ID, note ID, parent revision ID(s), encrypted snapshot, author device, creation time.
- `DeletionTombstone`: note ID, deleting admin, deletion time, policy/version information.
- `TrashEntry`: note ID, trashing admin, trashed time, retention deadline, restore/purge state.
- `SharedToPrivateJob`: shared note, initiating admin, target Android device, status, timestamps, opaque acknowledgement, and failure category.
- `ActivityEvent`: event ID, type, time, origin, actor, target, outcome, encrypted metadata, expiry time.
- `SyncReceipt`: peer, object/revision ID, direction, outcome, and time required for idempotency and diagnostics.
- `HouseholdPolicy`: enrollment mode, revision limit, activity retention, trash retention, version, changing admin, effective time.
- `DevicePresence`: registered device, authenticated session, last heartbeat, derived status, network and discovery metadata, and observation time.
- `ReconciliationRun`: start/end time, peer, items exchanged, conflicts, outcome, and safe failure category.
- `AdminPasskeyCredential`: admin member, WebAuthn credential ID, public key, signature counter state, transports, creation/last-used time, and active/revoked status; never authenticator private-key material.
- Web-device, web-session, and web-access-policy records are defined in [web-requirements.md](web-requirements.md#8-core-data-requirements).

All IDs exchanged between peers shall be globally unique and shall not depend only on a device wall clock.

## 9. Non-functional requirements

- **NFR-001:** Core note creation, editing, and reading shall work with no internet connection.
- **NFR-002:** The app shall remain usable while no peer is reachable.
- **NFR-003:** Synchronization shall converge after registered devices repeatedly exchange valid changes.
- **NFR-004:** Background work shall respect Android battery and execution constraints.
- **NFR-005:** No synchronization error shall cause silent loss of a locally saved note revision.
- **NFR-006:** Network and cryptographic errors shall be presented in user-readable language while technical details remain available in diagnostics.
- **NFR-007:** UI controls shall support Android accessibility semantics, scalable text, and adequate contrast.
- **NFR-008:** The app shall display timestamps in the user's locale while retaining an unambiguous internal time representation.
- **NFR-009:** Administrative dashboards shall state that connection and activity information may be delayed until peer synchronization completes.
- **NFR-010:** The web admin interface shall be responsive for supported desktop and tablet browsers.
- **NFR-011:** The web companion shall not require installing the Android application on the computer used for administration.
- **NFR-012:** PostgreSQL restart or laptop-backend downtime shall not cause acknowledged Android revisions to be lost.
- **NFR-013:** After backend restart, discovery shall resume and reconciliation shall converge PostgreSQL with reachable peers without requiring manual re-entry.
- **NFR-014:** The global presence dashboard shall show the backend observation time and shall never portray stale presence as live.
- **NFR-015:** Database schema changes shall use versioned, forward-validated migrations. Automated PostgreSQL backup and disaster recovery are outside the current version 1 plan.

## 10. Known constraints and trust boundaries

- Open home-LAN registration is convenient but does not prevent a guest on that LAN from registering while the laptop discovery service is offering enrollment. This risk is explicitly accepted for open mode; admins can select approval-required mode when it is unacceptable.
- A copied Wi-Fi name (SSID) is not sufficient proof of the home LAN. The laptop uses a stored home-LAN profile plus an authenticated household exchange before offering registration. Without a trusted router identity, however, the LAN fingerprint remains a convenience boundary that a capable attacker may spoof.
- Some routers isolate Wi-Fi clients or suppress multicast discovery; automatic discovery may not work on those networks.
- Android may delay background work. “Automatic” means best effort under Android's supported execution model, not permanent real-time availability.
- Peer-provided timestamps and “note opened” events are reports from cooperating apps. A modified or compromised client can lie or omit events.
- Enrollment-policy updates are eventually consistent. A registered peer that was offline during a change may briefly enforce its older known policy after reconnecting until it receives the newer signed policy; the admin page must expose policy synchronization state.
- Data already decrypted or copied by a device cannot be remotely recalled after revocation, private copying, or shared-note deletion.
- No globally complete live admin view exists while devices are offline; the dashboard becomes complete as events propagate.
- The root-admin console is loopback-only by default, avoiding LAN delivery of privileged JavaScript. Enabling web access from another computer requires a separately trusted HTTPS design; an untrusted self-signed certificate is not sufficient.
- If the web companion or Java backend is not running or reachable, admin approvals, deletion, revocation, policy changes, PostgreSQL persistence, and global presence cannot be performed. Normal Android note editing and peer synchronization must continue.
- The discovery service cannot maintain or display live global presence while the laptop is powered off. Previously stored `Last seen` values remain available after restart.
- PostgreSQL is a full shared-data replica and control-plane authority, but it is not allowed to become a single point of failure for Android note usage.
- Version 1 has no password fallback or automated passkey/root-key/database recovery. Loss of every root-admin passkey or the laptop key material may permanently remove administrative access or the PostgreSQL replica.

## 11. Version 1 pages

The detailed page specification is in [design.md](design.md).

### 11.1 Android member app

1. Launch and app-lock page.
2. Create-or-join household page.
3. Member and device registration page.
4. Pending-registration page when approval is required.
5. Notes home page with private/shared filters.
6. Note editor page.
7. Note revision-history page.
8. Conflict-resolution page.
9. Synchronization status and diagnostics page.
10. Member profile and application settings page.

No admin page, admin navigation item, or privileged admin control shall exist in the Android app.

### 11.2 Web companion

The member and laptop page requirements are specified in [web-requirements.md](web-requirements.md). The web companion additionally provides these admin-only pages:

1. Admin authentication and reauthentication page.
2. Admin overview page.
3. Device and pending-registration management page.
4. Activity-history page.
5. Shared-note management page.
6. Household policy and admin-role settings page.

## 12. Explicitly out of scope for version 1

- Cloud accounts, cloud backup, or internet relay synchronization.
- Making the laptop backend a single point of failure for Android note reading, editing, or peer synchronization.
- Real-time, character-level collaborative editing.
- Selective sharing with only certain members or devices.
- Read-only shared-note roles.
- Attachments and media files.
- Native desktop or iOS applications; the browser-based web companion is the version 1 laptop surface.
- Any admin UI or admin-only action initiated from the Android app.
- Guaranteed remote erasure from revoked devices.
- Backfilling old content revisions to newly registered devices.
- Closed-browser push or operating-system notifications for admin events.
- Internet-global presence or discovery when devices cannot reach the laptop control plane.
- Password-based admin login or password fallback for passkeys.
- Automated root-key recovery, PostgreSQL backup, or standby control-plane failover.

## 13. Release acceptance summary

Version 1 is acceptable when the laptop Spring Boot backend initializes PostgreSQL and the root-admin household, serves the React web companion, and authenticates admins with passkeys. The shared-note page must initially list 20 matching notes, load 20 more on request, and apply the defined filters. Registered Android devices must continue working and peer-synchronizing while the backend is stopped, and PostgreSQL must reconcile without data loss after restart. The running discovery service must provide an honest globally aggregated presence view. An authenticated web admin must manage enrollment, roles, policies, activity retention, trash, restore/purge, and coordinated shared-to-private delivery. Android must contain no admin interface, and private notes must remain device-local.

## 14. Settled and deferred decisions

The following decisions are settled:

- Admin UI and privileged actions exist only in the web companion.
- Android remains a member note client and a validator/forwarder of signed administrative state.
- The web companion continues to support ordinary shared-note use from a laptop; it is not admin-only.
- The backend runs on the designated admin laptop.
- The backend uses Spring Boot, is stateful, and keeps the complete shared household dataset in PostgreSQL.
- The repository currently uses Spring Boot 4.1 and Java 25; the supported baseline is Java 21 or newer.
- Frontend and backend live in `/Users/hitstudio/Learnings/shared-notebook`.
- No web unit tests are required.
- The browser requires no PWA, extension, or desktop-app installation.
- Accepted web devices reconnect without repeated approval.
- Core use must not require an internet service.
- The laptop is the root admin and may delegate the admin role to another registered identity.
- The laptop discovery service stores registered devices and aggregates global presence while running.
- Android notes and P2P synchronization continue when the backend is stopped; web/admin/database/global-presence functions pause.
- All runtime options have documented `application.yaml` keys; runtime admin overrides are persisted in PostgreSQL.
- Shared-note deletion uses admin-managed trash with a default 30-day retention.
- Activity retention defaults to 100 days and is admin-configurable.
- Coordinated shared-to-private delivery is required.
- Closed-browser system notifications are not required.
- The admin console is loopback-only by default. A localhost origin is treated as potentially trustworthy by modern browsers; LAN browser access remains separately gated on trusted HTTPS.
- React with TypeScript is the version 1 web UI. The React production bundle is served by Spring Boot and communicates through versioned JSON endpoints.
- Admin authentication uses passkeys with no version 1 password fallback.
- Open enrollment relies only on presence on the configured home Wi-Fi. This convenience boundary and its Wi-Fi-guest risk are accepted for version 1.
- The web shared-note list loads 20 notes initially and 20 more for each `Show more` action.
- Search filters include title, creator, last editor, modified-date range, and conflict state. Filtering is performed over authorized decrypted data in backend memory without a plaintext PostgreSQL search index.

The previously listed questions about native mobile administration, internet-wide presence, non-local browser HTTPS, key/database recovery, delegated-admin promotion, laptop autostart/standby, and offline private-delivery targeting are intentionally deferred and do not block the current design. Existing safe defaults remain in force: administration stays web-only, presence is limited to devices reporting to the laptop, LAN web access is disabled, delegated admins cannot grant admin by default, and private-delivery jobs may remain pending.

## 15. Implementation status and technical gaps

As of the current development baseline, the following functional requirements are **not yet implemented** in the Android application:

- **FR-004 / FR-064 (Discovery):** Automated LAN discovery via mDNS/DNS-SD is not yet implemented.
- **FR-142 (Heartbeats):** Authenticated presence heartbeats are not yet sent by the Android client.
- **FR-050 to FR-063 (Peer Sync):** The peer-to-peer synchronization coordinator and encrypted session logic are in the design phase.
- **FR-005 to FR-016 (Registration):** The formal identity handshake and registration persistence are bypassed in the current build.

Consequently, the "Connected" status in the web companion's global presence dashboard (FR-144) will remain offline until the discovery and heartbeat mechanisms are completed.
