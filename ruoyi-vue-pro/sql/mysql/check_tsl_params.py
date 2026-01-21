#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json

tsl_file = "F:/work/ch_ibms/docs/sessions/session_20251025_dahua_camera_thing_model.json"
with open(tsl_file, 'r', encoding='utf-8') as f:
    tsl = json.load(f)

print("="*80)
print("服务参数检查")
print("="*80)
for s in tsl['services']:
    input_count = len(s.get('inputParams', []))
    output_count = len(s.get('outputParams', []))
    print(f"{s['name']:12s}: input={input_count}, output={output_count}")

print("\n" + "="*80)
print("事件参数检查")
print("="*80)
for e in tsl['events']:
    output_count = len(e.get('outputParams', []))
    event_type = e.get('eventType', 'N/A')
    print(f"{e['name']:12s}: type={event_type:6s}, output={output_count}")

















