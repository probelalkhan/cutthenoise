#!/usr/bin/env python3
"""
Magic QA Bridge — Firestore → androidTest file writer.

Watches the 'generated_tests' collection in Firestore for new documents.
Each document contains: { pkg, file, code }
The bridge converts the package to a directory path and writes the test file
into app/src/androidTest/java/<pkg-as-path>/<file>.

Usage:
    pip install -r requirements.txt
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
    python3 bridge.py
"""

import os
import sys
import threading
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore


# ---------- Configuration ----------

# Project root is the directory containing this script
PROJECT_ROOT = Path(__file__).resolve().parent
ANDROID_TEST_BASE = PROJECT_ROOT / "app" / "src" / "androidTest" / "java"


# ---------- Helpers ----------

def pkg_to_path(pkg: str) -> Path:
    """Convert dot-notation package to a directory path."""
    return Path(*pkg.split("."))


def write_test_file(pkg: str, filename: str, code: str) -> Path:
    """Write a test file to the correct androidTest package directory."""
    target_dir = ANDROID_TEST_BASE / pkg_to_path(pkg)
    target_dir.mkdir(parents=True, exist_ok=True)

    target_file = target_dir / filename
    target_file.write_text(code, encoding="utf-8")
    return target_file


# ---------- Firestore Listener ----------

def on_snapshot(col_snapshot, changes, read_time):
    """Handle Firestore collection changes."""
    for change in changes:
        if change.type.name == "ADDED":
            doc = change.document
            data = doc.to_dict()

            pkg = data.get("pkg")
            filename = data.get("file")
            code = data.get("code")

            if not all([pkg, filename, code]):
                print(f"⚠️  Skipping doc {doc.id}: missing pkg/file/code fields")
                continue

            try:
                target = write_test_file(pkg, filename, code)
                print(f"✅ Written: {target.relative_to(PROJECT_ROOT)}")
            except Exception as e:
                print(f"❌ Error writing {filename}: {e}")


def main():
    print("=" * 60)
    print("  Magic QA Bridge — Firestore → androidTest")
    print("=" * 60)
    print(f"  Project root  : {PROJECT_ROOT}")
    print(f"  Test output   : {ANDROID_TEST_BASE}")
    print()

    # Initialize Firebase Admin
    cred_path = os.environ.get("GOOGLE_APPLICATION_CREDENTIALS")
    if cred_path:
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
        print(f"  Firebase cred : {cred_path}")
    else:
        # Try default credentials (e.g., ADC or emulator)
        firebase_admin.initialize_app()
        print("  Firebase cred : default (ADC)")

    print()
    print("👁️  Watching Firestore 'generated_tests' collection...")
    print("    (Press Ctrl+C to stop)")
    print()

    db = firestore.client()
    col_ref = db.collection("generated_tests")

    # Start real-time listener
    query_watch = col_ref.on_snapshot(on_snapshot)

    # Keep the main thread alive
    stop_event = threading.Event()
    try:
        stop_event.wait()
    except KeyboardInterrupt:
        print("\n🛑 Bridge stopped.")
        query_watch.unsubscribe()
        sys.exit(0)


if __name__ == "__main__":
    main()
