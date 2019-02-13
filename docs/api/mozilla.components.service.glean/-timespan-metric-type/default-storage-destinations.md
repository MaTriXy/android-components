[android-components](../../index.md) / [mozilla.components.service.glean](../index.md) / [TimespanMetricType](index.md) / [defaultStorageDestinations](./default-storage-destinations.md)

# defaultStorageDestinations

`val defaultStorageDestinations: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`>` [(source)](https://github.com/mozilla-mobile/android-components/blob/master/components/service/glean/src/main/java/mozilla/components/service/glean/TimespanMetricType.kt#L28)

Overrides [CommonMetricData.defaultStorageDestinations](../-common-metric-data/default-storage-destinations.md)

Defines the names of the storages the metric defaults to when
"default" is used as the destination storage.
Note that every metric type will need to override this.
