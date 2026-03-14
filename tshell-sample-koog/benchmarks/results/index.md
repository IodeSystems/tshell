# Benchmark Results

**Model:** Qwen3-5-35B-A3B
**Date:** 2026-03-14T11:05:00.933441544
**Score:** 30/32

| Teaser | Status | Tool Calls | Errors | Duration | Details |
|--------|--------|-----------|--------|----------|---------|
| factorial | PASS | 1 | 0 | 20039ms | [detail](factorial.md) |
| fizzbuzz | PASS | 1 | 0 | 8387ms | [detail](fizzbuzz.md) |
| closure_counter | PASS | 1 | 0 | 7650ms | [detail](closure_counter.md) |
| pipe_chain | FAIL | 9 | 0 | 30005ms | [detail](pipe_chain.md) (TIMEOUT (30s)) |
| recursive_flatten | PASS | 1 | 0 | 20366ms | [detail](recursive_flatten.md) |
| object_transform | FAIL | 1 | 0 | 8068ms | [detail](object_transform.md) |
| string_manipulation | PASS | 1 | 0 | 4665ms | [detail](string_manipulation.md) |
| reduce_groupby | PASS | 1 | 0 | 8482ms | [detail](reduce_groupby.md) |
| bitwise_flags | PASS | 1 | 0 | 11050ms | [detail](bitwise_flags.md) |
| scatter_parallel | PASS | 1 | 0 | 7263ms | [detail](scatter_parallel.md) |
| fibonacci_memo | PASS | 2 | 0 | 13533ms | [detail](fibonacci_memo.md) |
| regex_extract | PASS | 2 | 0 | 9915ms | [detail](regex_extract.md) |
| matrix_multiply | PASS | 1 | 0 | 11151ms | [detail](matrix_multiply.md) |
| deep_clone | PASS | 1 | 0 | 14878ms | [detail](deep_clone.md) |
| binary_search | PASS | 2 | 0 | 20732ms | [detail](binary_search.md) |
| curry | PASS | 2 | 0 | 9354ms | [detail](curry.md) |
| linked_list | PASS | 1 | 0 | 10225ms | [detail](linked_list.md) |
| pipe_wordfreq | PASS | 1 | 0 | 7804ms | [detail](pipe_wordfreq.md) |
| roman_numerals | PASS | 1 | 0 | 15630ms | [detail](roman_numerals.md) |
| merge_sort | PASS | 1 | 0 | 14522ms | [detail](merge_sort.md) |
| event_emitter | PASS | 2 | 0 | 19361ms | [detail](event_emitter.md) |
| pipe_csv_parse | PASS | 3 | 0 | 22720ms | [detail](pipe_csv_parse.md) |
| count_letter_r_strawberry | PASS | 1 | 0 | 4985ms | [detail](count_letter_r_strawberry.md) |
| count_letter_l_lullaby | PASS | 1 | 0 | 3804ms | [detail](count_letter_l_lullaby.md) |
| reverse_words_order | PASS | 1 | 0 | 5497ms | [detail](reverse_words_order.md) |
| anagram_check | PASS | 1 | 0 | 9534ms | [detail](anagram_check.md) |
| nth_prime | PASS | 1 | 0 | 7867ms | [detail](nth_prime.md) |
| collatz_steps | PASS | 1 | 0 | 6102ms | [detail](collatz_steps.md) |
| digit_sum_power | PASS | 2 | 0 | 11507ms | [detail](digit_sum_power.md) |
| longest_common_subsequence | PASS | 1 | 0 | 12390ms | [detail](longest_common_subsequence.md) |
| balanced_parens | PASS | 1 | 0 | 11642ms | [detail](balanced_parens.md) |
| tower_of_hanoi | PASS | 1 | 0 | 6945ms | [detail](tower_of_hanoi.md) |

## Summary

Failed: pipe_chain, object_transform

## Aggregate Stats

| Metric | Value |
|--------|-------|
| Pass rate | 93% (30/32) |
| First-try success | 23/32 |
| Total tool calls | 48 |
| Tool errors | 0 |
| Avg tool calls/teaser | 1.5 |
| Total time | 376s |
| Avg time/teaser | 11s |
| Error recovery rate | N/A (no errors) |
