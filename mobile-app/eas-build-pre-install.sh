#!/bin/bash
# EAS Build pre-install hook
# Delete the lock file so npm install (not npm ci) will be used
# This avoids lockfile version mismatch between local npm 11 and EAS npm 10
rm -f package-lock.json
