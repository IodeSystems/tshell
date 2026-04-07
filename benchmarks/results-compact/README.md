# Benchmark Results

**Model:** Qwen3-5-35B-A3B-APEX
**Date:** 2026-04-07T01:14:12.265611114
**Score:** 31/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 6816ms | [detail](Qwen3-5-35B-A3B-APEX/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6328ms | [detail](Qwen3-5-35B-A3B-APEX/fizzbuzz.md) |
| closure_counter | PASS | 0 | 0 | 1806ms | [detail](Qwen3-5-35B-A3B-APEX/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4818ms | [detail](Qwen3-5-35B-A3B-APEX/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 6710ms | [detail](Qwen3-5-35B-A3B-APEX/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5011ms | [detail](Qwen3-5-35B-A3B-APEX/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3874ms | [detail](Qwen3-5-35B-A3B-APEX/string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7197ms | [detail](Qwen3-5-35B-A3B-APEX/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5571ms | [detail](Qwen3-5-35B-A3B-APEX/bitwise_flags.md) |
| scatter_parallel | PASS | 0 | 0 | 1945ms | [detail](Qwen3-5-35B-A3B-APEX/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 6762ms | [detail](Qwen3-5-35B-A3B-APEX/fibonacci_memo.md) |
| regex_extract | PASS | 2 | 0 | 5938ms | [detail](Qwen3-5-35B-A3B-APEX/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 5896ms | [detail](Qwen3-5-35B-A3B-APEX/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9022ms | [detail](Qwen3-5-35B-A3B-APEX/deep_clone.md) |
| binary_search | PASS | 1 | 0 | 6504ms | [detail](Qwen3-5-35B-A3B-APEX/binary_search.md) |
| curry | PASS | 1 | 0 | 5047ms | [detail](Qwen3-5-35B-A3B-APEX/curry.md) |
| linked_list | PASS | 1 | 0 | 6448ms | [detail](Qwen3-5-35B-A3B-APEX/linked_list.md) |
| pipe_wordfreq | FAIL | 1 | 0 | 5097ms | [detail](Qwen3-5-35B-A3B-APEX/pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 9514ms | [detail](Qwen3-5-35B-A3B-APEX/roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 12245ms | [detail](Qwen3-5-35B-A3B-APEX/merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 7290ms | [detail](Qwen3-5-35B-A3B-APEX/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 9159ms | [detail](Qwen3-5-35B-A3B-APEX/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3599ms | [detail](Qwen3-5-35B-A3B-APEX/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3561ms | [detail](Qwen3-5-35B-A3B-APEX/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 4005ms | [detail](Qwen3-5-35B-A3B-APEX/count_words_with_letter.md) |
| anagram_check | FAIL | 5 | 5 | 30005ms | [detail](Qwen3-5-35B-A3B-APEX/anagram_check.md) (TIMEOUT (30s)) |
| nth_prime | PASS | 1 | 0 | 9921ms | [detail](Qwen3-5-35B-A3B-APEX/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5455ms | [detail](Qwen3-5-35B-A3B-APEX/collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 4233ms | [detail](Qwen3-5-35B-A3B-APEX/digit_sum_power.md) |
| longest_common_subsequence | PASS | 2 | 1 | 17637ms | [detail](Qwen3-5-35B-A3B-APEX/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6830ms | [detail](Qwen3-5-35B-A3B-APEX/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3439ms | [detail](Qwen3-5-35B-A3B-APEX/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 7157ms | [detail](Qwen3-5-35B-A3B-APEX/escape_heavy_strings.md) |

## Summary

Failed: pipe_wordfreq, anagram_check

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 93% (31/33) |
| First-try success | 27/33 |
| Total tool calls | 37 |
| Tool errors | 6 |
| Avg tool calls/teaser | 1.1 |
| Total time | 234s |
| Avg time/teaser | 7s |
| Error recovery rate | 1/6 recovered |
