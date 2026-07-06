"""
Question 2 (Python port): Read 3 text files with one thread per file, and
have each thread write its contents DIRECTLY into a single shared summary
file.

See ../multithread-file-writing/question.md for the full problem statement.

Core idea: reading is thread-local work (no shared state), but writing
touches a SHARED resource (the summary file), so that part must be
synchronized to avoid interleaved/corrupted output.

Note on the GIL: Python's Global Interpreter Lock does NOT make this lock
unnecessary. The GIL only ensures one thread executes Python bytecode at a
time; it does not protect multi-step operations (like "write a header, then
loop and write N lines") as a single atomic unit, and file I/O releases the
GIL while blocked on the underlying write() syscall. Two threads can still
interleave their writes without an explicit lock.
"""

import threading
from pathlib import Path


def write_summary(input_files: list[Path], summary_file: Path) -> None:
    write_lock = threading.Lock()  # guards the shared writer

    with summary_file.open("w") as writer:

        def worker(file: Path) -> None:
            # Reading is thread-local work: no shared state, no lock needed.
            lines = file.read_text().splitlines()

            # Writing touches the SHARED writer -> must be synchronized so
            # one thread's block of lines can't get interleaved with another's.
            with write_lock:
                writer.write(f"=== {file.name} ===\n")
                for line in lines:
                    writer.write(line + "\n")

        threads = [threading.Thread(target=worker, args=(f,)) for f in input_files]
        for t in threads:
            t.start()
        for t in threads:
            t.join()


def main() -> None:
    directory = Path(".")
    input1 = directory / "input1.txt"
    input2 = directory / "input2.txt"
    input3 = directory / "input3.txt"

    input1.write_text("Alpha line one\nAlpha line two\n")
    input2.write_text("Beta line one\nBeta line two\n")
    input3.write_text("Gamma line one\nGamma line two\n")

    inputs = [input1, input2, input3]
    summary = directory / "summary.txt"

    write_summary(inputs, summary)

    print("=== summary.txt (written concurrently by 3 threads) ===")
    print(summary.read_text(), end="")


if __name__ == "__main__":
    main()
