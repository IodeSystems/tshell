# Benchmark Results

**Model:** Qwen3-5-35B-A3B-APEX-I-Balanced
**Date:** 2026-04-07T02:13:01.912212354
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 10586ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6929ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 5313ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 5721ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6791ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5970ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4439ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 8666ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 6637ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 4696ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 6754ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 5820ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 9073ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 11107ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/deep_clone.md) |
| binary_search | PASS | 1 | 0 | 8849ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/binary_search.md) |
| curry | PASS | 1 | 0 | 5807ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/curry.md) |
| linked_list | PASS | 1 | 0 | 6925ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 5525ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 11188ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 13123ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/merge_sort.md) |
| event_emitter | PASS | 3 | 2 | 18913ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 9675ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3551ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3641ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 4146ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4696ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7348ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5593ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4329ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 10552ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 8014ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3406ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 7811ms | [detail](Qwen3-5-35B-A3B-APEX-I-Balanced/escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 32/33 |
| Total tool calls | 35 |
| Tool errors | 2 |
| Avg tool calls/teaser | 1.1 |
| Total time | 241s |
| Avg time/teaser | 7s |
| Error recovery rate | 1/2 recovered |
