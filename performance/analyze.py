"""Evaluate a measured window of HTTP samples; Python 3, no dependencies."""
import argparse
import csv
import json
import math
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("jtl", type=Path)
parser.add_argument("--start-ms", type=int, required=True, help="Absolute inclusive timestamp of measurement window")
parser.add_argument("--seconds", type=float, required=True)
parser.add_argument("--target-rps", type=float, default=250)
args = parser.parse_args()
if args.seconds <= 0 or args.target_rps <= 0:
    parser.error("seconds and target-rps must be positive")
with args.jtl.open(newline="", encoding="utf-8") as source:
    samples = [row for row in csv.DictReader(source)
               if row["label"].startswith("HTTP_")
               and args.start_ms <= int(row["timeStamp"]) < args.start_ms + args.seconds * 1000]
if not samples:
    raise SystemExit("No HTTP samples in the selected window; criterion unverified")
times = sorted(int(row["elapsed"]) for row in samples)
p90 = times[math.ceil(len(times) * .9) - 1]
errors = sum(row["success"].lower() != "true" for row in samples)
rps = len(samples) / args.seconds
result = {
    "window_start_ms": args.start_ms, "window_seconds": args.seconds,
    "requests": len(samples), "rps": rps, "p90_ms": p90,
    "errors": errors, "error_percent": errors / len(samples) * 100,
    "successful_purchases": sum(row["label"] == "HTTP_04_Confirmar" and row["success"] == "true" for row in samples),
    "criterion_met": rps >= args.target_rps and p90 < 2000,
    "functional_check_passed": errors == 0,
    "percentile_method": "nearest rank; all HTTP samples, including failures",
}
print(json.dumps(result, indent=2))
raise SystemExit(0 if result["criterion_met"] and result["functional_check_passed"] else 1)
