import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from validate_parts_manifest import audit


class ManifestAuditTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / "images").mkdir()
        self.image = self.root / "images" / "part.jpg"
        self.image.write_bytes(b"sample-image-bytes")
        self.digest = hashlib.sha256(self.image.read_bytes()).hexdigest()
        self.manifest = self.root / "manifest.jsonl"

    def tearDown(self):
        self.temp.cleanup()

    def record(self, **overrides):
        row = {
            "sample_id": "s1", "source_dataset": "test-source",
            "source_version": "1", "source_url": "https://example.org/data",
            "licence_id": "CC-BY-4.0", "original_file_sha256": self.digest,
            "relative_path": "images/part.jpg", "task_type": "component_family",
            "annotation_type": "image_label", "component_label_original": "bearing",
            "component_label_canonical": "bearing",
            "capture_group_id": "machine-01-session-01", "split": "train",
            "is_original_or_augmented": "original",
        }
        row.update(overrides)
        return row

    def write(self, rows):
        self.manifest.write_text("\n".join(json.dumps(x) for x in rows), encoding="utf-8")

    def test_valid_manifest(self):
        self.write([self.record()])
        errors, report = audit(self.manifest, self.root)
        self.assertEqual([], errors)
        self.assertEqual(1, report["valid_file_hashes"])

    def test_capture_group_split_leakage(self):
        self.write([self.record(), self.record(sample_id="s2", split="test")])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("leaks across splits" in error for error in errors))

    def test_hash_mismatch(self):
        self.write([self.record(original_file_sha256="0" * 64)])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("SHA-256 mismatch" in error for error in errors))

    def test_augmented_requires_parent(self):
        self.write([self.record(is_original_or_augmented="augmented")])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("parent_sample_id" in error for error in errors))

    def test_augmented_parent_must_exist(self):
        self.write([self.record(is_original_or_augmented="augmented", parent_sample_id="missing")])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("does not exist in manifest" in error for error in errors))

    def test_augmented_parent_must_share_split_and_capture_group(self):
        parent = self.record()
        augmented_path = self.root / "images" / "aug.jpg"
        augmented_path.write_bytes(b"different-augmented-image")
        augmented = self.record(
            sample_id="s2", relative_path="images/aug.jpg",
            original_file_sha256=hashlib.sha256(augmented_path.read_bytes()).hexdigest(),
            is_original_or_augmented="augmented", parent_sample_id="s1",
            split="test", capture_group_id="different-session",
        )
        self.write([parent, augmented])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("same split" in error for error in errors))
        self.assertTrue(any("share capture_group_id" in error for error in errors))

    def test_task_annotation_mismatch(self):
        self.write([self.record(task_type="instance_retrieval", annotation_type="image_label")])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("incompatible" in error for error in errors))

    def test_path_traversal_rejected(self):
        self.write([self.record(relative_path="../outside.jpg")])
        errors, _ = audit(self.manifest, self.root)
        self.assertTrue(any("stay inside image_root" in error for error in errors))

    def test_empty_manifest_rejected(self):
        self.write([])
        errors, report = audit(self.manifest, self.root)
        self.assertTrue(any("no JSON records" in error for error in errors))
        self.assertEqual(0, report["records"])


if __name__ == "__main__":
    unittest.main()
