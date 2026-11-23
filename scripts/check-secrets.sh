#!/usr/bin/env bash
#
# Bootscope secret check.
#
# Scans files for things that look like committed secrets (private keys, cloud
# credentials, tokens, JWTs, password/secret assignments, credential-bearing
# connection strings). Used in two places:
#
#   * the pre-commit hook (scans STAGED changes)      -> scripts/check-secrets.sh --staged
#   * CI                     (scans the whole tree)   -> scripts/check-secrets.sh --all
#
# Exit 0 = clean, Exit 1 = a likely secret was found (commit/CI should fail).
#
# This is defence in depth, not a guarantee. Obvious placeholders (changeme,
# your-*, example, ${...}, xxxx, redacted) are ignored to keep noise down.

set -euo pipefail

mode="${1:---staged}"

# --- Collect the list of files to scan -------------------------------------
case "$mode" in
  --staged)
    mapfile -t files < <(git diff --cached --name-only --diff-filter=ACM)
    ;;
  --all)
    mapfile -t files < <(git ls-files)
    ;;
  *)
    echo "usage: $0 [--staged|--all]" >&2
    exit 2
    ;;
esac

# Files we never scan (self, license text, binaries handled below).
skip_re='^(LICENSE|scripts/check-secrets\.sh)$'

# --- Secret patterns (extended regex) --------------------------------------
# Each entry: a human label, then the regex.
patterns=(
  "Private key block|-----BEGIN ([A-Z ]+ )?PRIVATE KEY-----"
  "AWS access key id|AKIA[0-9A-Z]{16}"
  "AWS secret access key|aws_secret_access_key[[:space:]]*=[[:space:]]*[A-Za-z0-9/+]{40}"
  "Google API key|AIza[0-9A-Za-z_-]{35}"
  "Slack token|xox[baprs]-[0-9A-Za-z-]{10,}"
  "GitHub token|gh[pousr]_[0-9A-Za-z]{36,}"
  "JWT|eyJ[A-Za-z0-9_-]{10,}\.eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}"
  "Credentialed URL|[a-zA-Z][a-zA-Z0-9+.-]*://[^/[:space:]:@]+:[^/[:space:]@]+@"
  "Password/secret/token assignment|(password|passwd|secret|token|api[_-]?key|access[_-]?key|private[_-]?key|credential)[\"' ]*[:=][[:space:]]*[\"']?[^\"'[:space:]$<#]{6,}"
)

# Values that are clearly NOT real secrets -> ignore the matching line.
# A line carrying the pragma `bootscope:allow-secret` is exempt: used for the test
# fixtures and the Redactor source, whose whole purpose is to hold secret-shaped text.
allow_re='(bootscope:allow-secret|changeme|change-me|your[-_]|example|placeholder|dummy|sample|redacted|REDACTED|xxx+|\*\*\*|\$\{[^}]+\}|<[^>]+>|password[\"'\'' ]*[:=][[:space:]]*[\"'\'']?(password|secret|pass)\b)'

found=0

for f in "${files[@]}"; do
  [[ -z "$f" ]] && continue
  [[ ! -f "$f" ]] && continue
  echo "$f" | grep -Eq "$skip_re" && continue
  # Skip binary files.
  if grep -Iq . "$f" 2>/dev/null; then :; else continue; fi

  for entry in "${patterns[@]}"; do
    label="${entry%%|*}"
    regex="${entry#*|}"
    while IFS= read -r line; do
      [[ -z "$line" ]] && continue
      # Drop lines that are obvious placeholders.
      if echo "$line" | grep -Eiq "$allow_re"; then continue; fi
      if [[ $found -eq 0 ]]; then
        echo "✖ Possible secret(s) found:" >&2
        echo >&2
      fi
      found=1
      echo "  [$label] $f:$line" >&2
    done < <(grep -nEi "$regex" "$f" 2>/dev/null || true)
  done
done

if [[ $found -ne 0 ]]; then
  echo >&2
  echo "Commit blocked. If this is a false positive, mask the value or use a placeholder" >&2
  echo "(e.g. \${ENV_VAR}, changeme, your-token-here), or scan with: scripts/check-secrets.sh --all" >&2
  exit 1
fi

echo "✓ Secret check passed (${mode})."
