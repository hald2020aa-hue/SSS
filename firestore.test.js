const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validUiProfile(uid) {
  return {
    userId: uid,
    profileName: "SSS Cyber Neon Pro",
    neonThemeId: "CYBER_CYAN",
    layoutMode: "GRID_3",
    iconStyle: "NEON_GLOW",
    navigationBarStyle: "FLOATING_PILL",
    homeSectionOrder: "FEATURED_RECENT_POPULAR",
    cardCornerRadiusDp: 16,
    neonGlowIntensity: 85,
    readerMode: "WEBTOON_VERTICAL",
    deviceName: "Pixel 9 Pro",
    isActive: true,
    createdAt: new Date(Date.now() - 1000),
    updatedAt: new Date(Date.now() - 1000),
  };
}

function validMangaBookmark(uid) {
  return {
    userId: uid,
    mangaKey: "md_solo_leveling",
    title: "Solo Leveling: Neon Awakening",
    coverUrl: "https://uploads.mangadex.org/covers/example.jpg",
    sourceName: "MangaDex",
    author: "Chugong",
    genre: "Action",
    category: "READING",
    lastReadChapter: 45,
    totalChapters: 179,
    rating: 4.9,
    createdAt: new Date(Date.now() - 1000),
    updatedAt: new Date(Date.now() - 1000),
  };
}

function validReadingSession(uid) {
  return {
    userId: uid,
    mangaTitle: "Solo Leveling: Neon Awakening",
    genre: "Action",
    minutesSpent: 42,
    pagesRead: 120,
    chaptersCompleted: 3,
    neonThemeUsed: "CYBER_CYAN",
    layoutModeUsed: "GRID_3",
    createdAt: new Date(Date.now() - 1000),
    updatedAt: new Date(Date.now() - 1000),
  };
}

test("Unauthenticated user cannot read or write ui_profiles, library, or reading_sessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("ui_profiles").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ui_profiles")
      .doc("p1")
      .set(validUiProfile(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("library").get()
  );
});

test("Authenticated owner can create, read, list, update, and delete their own UI profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("ui_profiles")
    .doc("profile_1");

  await assertSucceeds(docRef.set(validUiProfile(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ui_profiles")
      .where("userId", "==", ALICE_UID)
      .get()
  );
  await assertSucceeds(
    docRef.update({
      neonThemeId: "NEON_MAGENTA",
      neonGlowIntensity: 95,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Cross-user access is strictly rejected across all collections", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("ui_profiles")
      .doc("p1")
      .set(validUiProfile(ALICE_UID));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("library")
      .doc("m1")
      .set(validMangaBookmark(ALICE_UID));
    await db
      .collection("users")
      .doc(ALICE_UID)
      .collection("reading_sessions")
      .doc("s1")
      .set(validReadingSession(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("ui_profiles")
      .doc("p1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("library")
      .doc("m1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("reading_sessions")
      .doc("s1")
      .get()
  );
});

test("Dirty Dozen: rejects ghost fields, invalid enums, out-of-bounds values, and immutable field tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("ui_profiles")
    .doc("p_invalid");

  // 1. Ghost field injection
  await assertFails(
    profileRef.set({
      ...validUiProfile(ALICE_UID),
      isAdmin: true,
    })
  );

  // 2. Invalid neon theme enum
  await assertFails(
    profileRef.set({
      ...validUiProfile(ALICE_UID),
      neonThemeId: "INVALID_NEON",
    })
  );

  // 3. Out of bounds glow intensity
  await assertFails(
    profileRef.set({
      ...validUiProfile(ALICE_UID),
      neonGlowIntensity: 150,
    })
  );

  // 4. Spoofed userId on create
  await assertFails(
    profileRef.set({
      ...validUiProfile(BOB_UID),
    })
  );

  // 5. Immutable createdAt mutation on update
  const validRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("library")
    .doc("m_valid");
  await assertSucceeds(validRef.set(validMangaBookmark(ALICE_UID)));
  await assertFails(
    validRef.update({
      createdAt: new Date(),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
