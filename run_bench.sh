#!/bin/bash
rm -rf shared/core/build/test-results/jvmTest/TEST-io.github.smithjustinn.domain.services.MatchEvaluatorPerfJvmTest.xml
./gradlew :shared:core:jvmTest --tests "*MatchEvaluatorPerfJvmTest*" --rerun-tasks > /dev/null 2>&1
cat shared/core/build/test-results/jvmTest/TEST-io.github.smithjustinn.domain.services.MatchEvaluatorPerfJvmTest.xml | grep -B 1 -A 1 BENCHMARK_RESULT
