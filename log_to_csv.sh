#!/usr/bin/env bash

cd "C:/Users/Lenovo/Cursor Workspace/Selenium-UI-Automation-ajSM" || exit 1

INPUT="gmail-api-2026-02-05.log"
OUTPUT="log_to_csv_test3.csv"

{
  echo "courier_slug,status"
  awk "
/courier_slug/ {
  # line like:   'courier_slug' => 'BLUEDART',
  split(\$0, a, \"'\")
  slug = a[4]
}
/status/ && slug != \"\" {
  # line like:   'status' => 'DELIVERY  SCHEDULED FOR NEXT WORKING DAY',
  split(\$0, a, \"'\")
  status = a[4]
  # CSV row with quotes
  printf \"\\\"%s\\\",\\\"%s\\\"\\n\", slug, status
  slug = \"\"
}
" "$INPUT"
} > "$OUTPUT"

echo "Created $OUTPUT"