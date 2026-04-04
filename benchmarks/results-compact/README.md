# Benchmark Results

**Model:** Qwen3-5-35B-A3B-turbo
**Date:** 2026-04-04T04:05:58.817666707
**Score:** 33/33

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 6260ms | [detail](Qwen3-5-35B-A3B-turbo/factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 6440ms | [detail](Qwen3-5-35B-A3B-turbo/fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 4494ms | [detail](Qwen3-5-35B-A3B-turbo/closure_counter.md) |
| pipe_chain | PASS | 1 | 0 | 4642ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_chain.md) |
| recursive_flatten | PASS | 1 | 0 | 5789ms | [detail](Qwen3-5-35B-A3B-turbo/recursive_flatten.md) |
| object_transform | PASS | 1 | 0 | 5010ms | [detail](Qwen3-5-35B-A3B-turbo/object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 3622ms | [detail](Qwen3-5-35B-A3B-turbo/string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 7316ms | [detail](Qwen3-5-35B-A3B-turbo/reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 5146ms | [detail](Qwen3-5-35B-A3B-turbo/bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 3582ms | [detail](Qwen3-5-35B-A3B-turbo/scatter_parallel.md) |
| fibonacci_memo | PASS | 1 | 0 | 5934ms | [detail](Qwen3-5-35B-A3B-turbo/fibonacci_memo.md) |
| regex_extract | PASS | 1 | 0 | 4151ms | [detail](Qwen3-5-35B-A3B-turbo/regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 7871ms | [detail](Qwen3-5-35B-A3B-turbo/matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 9066ms | [detail](Qwen3-5-35B-A3B-turbo/deep_clone.md) |
| binary_search | PASS | 1 | 0 | 6433ms | [detail](Qwen3-5-35B-A3B-turbo/binary_search.md) |
| curry | PASS | 1 | 0 | 3643ms | [detail](Qwen3-5-35B-A3B-turbo/curry.md) |
| linked_list | PASS | 2 | 1 | 9318ms | [detail](Qwen3-5-35B-A3B-turbo/linked_list.md) |
| pipe_wordfreq | PASS | 4 | 1 | 13114ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_wordfreq.md) |
| roman_numerals | PASS | 0 | 0 | 1762ms | [detail](Qwen3-5-35B-A3B-turbo/roman_numerals.md) |
| merge_sort | PASS | 2 | 1 | 21183ms | [detail](Qwen3-5-35B-A3B-turbo/merge_sort.md) |
| event_emitter | PASS | 1 | 0 | 6436ms | [detail](Qwen3-5-35B-A3B-turbo/event_emitter.md) |
| pipe_csv_parse | PASS | 1 | 0 | 8813ms | [detail](Qwen3-5-35B-A3B-turbo/pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 3123ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 5 | 2 | 12738ms | [detail](Qwen3-5-35B-A3B-turbo/count_letter_l_lullaby.md) |
| count_words_with_letter | PASS | 1 | 0 | 3371ms | [detail](Qwen3-5-35B-A3B-turbo/count_words_with_letter.md) |
| anagram_check | PASS | 1 | 0 | 4119ms | [detail](Qwen3-5-35B-A3B-turbo/anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 6594ms | [detail](Qwen3-5-35B-A3B-turbo/nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 5681ms | [detail](Qwen3-5-35B-A3B-turbo/collatz_steps.md) |
| digit_sum_power | PASS | 1 | 0 | 3788ms | [detail](Qwen3-5-35B-A3B-turbo/digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 8924ms | [detail](Qwen3-5-35B-A3B-turbo/longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 6450ms | [detail](Qwen3-5-35B-A3B-turbo/balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 3014ms | [detail](Qwen3-5-35B-A3B-turbo/tower_of_hanoi.md) |
| escape_heavy_strings | PASS | 1 | 0 | 7596ms | [detail](Qwen3-5-35B-A3B-turbo/escape_heavy_strings.md) |

## Summary

All benchmarks passed.

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 100% (33/33) |
| First-try success | 28/33 |
| Total tool calls | 41 |
| Tool errors | 5 |
| Avg tool calls/teaser | 1.2 |
| Total time | 215s |
| Avg time/teaser | 6s |
| Error recovery rate | 4/5 recovered |
