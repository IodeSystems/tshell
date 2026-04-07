# Benchmark Results

**Model:** Qwen3-5-35B-A3B-APEX-I-Balanced
**Date:** 2026-04-07T07:09:31.931372662
**Score:** 32/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 0 | 0 | 20392ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 7204ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5281ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 5442ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6055ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5339ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4046ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 8751ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 6112ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/bitwise_flags.md) |
| scatter_parallel | PASS | 0 | 0 | 2029ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 6602ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 4744ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 6705ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 10223ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/deep_clone.md) |
| binary_search | PASS | 2 | 1 | 13991ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/binary_search.md) |
| curry | PASS | 1 | 0 | 4212ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/curry.md) |
| linked_list | PASS | 1 | 0 | 6865ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/linked_list.md) |
| pipe_wordfreq | PASS | 7 | 1 | 27463ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 10946ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/roman_numerals.md) |
| merge_sort | FAIL | 3 | 2 | 30006ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/merge_sort.md) (TIMEOUT (30s)) |
| event_emitter | PASS | 1 | 0 | 9762ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 8951ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3656ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3460ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3892ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4850ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/anagram_check.md) |
| nth_prime | PASS | 2 | 1 | 13498ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5214ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/collatz_steps.md) |
| digit_sum_power | PASS | 2 | 1 | 7546ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 10382ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 7114ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3129ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 3 | 2 | 20222ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/escape_heavy_strings.md) |

## Summary

Failed: merge_sort

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 96% (32/33) |
| First-try success | 25/33 |
| Total tool calls | 44 |
| Tool errors | 8 |
| Avg tool calls/teaser | 1.3 |
| Total time | 294s |
| Avg time/teaser | 8s |
| Error recovery rate | 5/8 recovered |
