#!/bin/sh -eu
cd "$(dirname "$0")/.."

xcodegen generate --spec project.yml --project .
