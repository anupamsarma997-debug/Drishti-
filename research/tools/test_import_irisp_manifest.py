import tempfile
import unittest
from pathlib import Path

from import_irisp_manifest import build_records


class IrispImporterTests(unittest.TestCase):
    def make_tree(self, root: Path) -> Path:
        for split, object_id, filename in (
            ("train", "obj-001", "a.jpg"),
            ("train", "obj-001", "b.png"),
            ("val", "obj-002", "c.jpg"),
            ("test", "obj-003", "d.webp"),
            ("query", "obj-004", "ignored.jpg"),
        ):
            folder = root / split / "phone" / object_id
            folder.mkdir(parents=True, exist_ok=True)
            (folder / filename).write_bytes(b"test-image-" + filename.encode())
        gallery = root / "gallery" / "studio" / "obj-005"
        gallery.mkdir(parents=True)
        (gallery / "ignored.jpg").write_bytes(b"gallery")
        return root

    def test_imports_only_official_train_val_test_phone_folders(self):
        with tempfile.TemporaryDirectory() as temp:
            records = build_records(self.make_tree(Path(temp) / "images"))
        self.assertEqual(len(records), 4)
        self.assertEqual({r["split"] for r in records}, {"train", "validation", "test"})
        self.assertTrue(all(r["source_dataset"] == "IRISP" for r in records))

    def test_object_id_is_instance_identity_not_semantic_label(self):
        with tempfile.TemporaryDirectory() as temp:
            records = build_records(self.make_tree(Path(temp) / "images"))
        first = records[0]
        self.assertEqual(first["task_type"], "instance_retrieval")
        self.assertEqual(first["annotation_type"], "instance_id")
        self.assertEqual(first["component_label_canonical"], "unknown")
        self.assertEqual(first["semantic_label_status"], "NOT_PROVIDED_BY_SOURCE")
        self.assertEqual(first["capture_group_id"], "irisp-object-obj-001")

    def test_records_have_hash_and_shared_object_group(self):
        with tempfile.TemporaryDirectory() as temp:
            records = build_records(self.make_tree(Path(temp) / "images"))
        same_object = [r for r in records if r["instance_id_original"] == "obj-001"]
        self.assertEqual(len(same_object), 2)
        self.assertEqual(len({r["capture_group_id"] for r in same_object}), 1)
        self.assertTrue(all(len(r["original_file_sha256"]) == 64 for r in records))

    def test_missing_root_fails(self):
        with tempfile.TemporaryDirectory() as temp:
            with self.assertRaises(FileNotFoundError):
                build_records(Path(temp) / "missing")


if __name__ == "__main__":
    unittest.main()
