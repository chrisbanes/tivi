# android

This is an image that contains the latest Android SDK and NDK, used to build Android projects.

## Examples

The following examples demonstrate build requests that use this builder.

### Fetch the contents of a remote URL

This `cloudbuild.yaml` extracts a zip archive, overwriting any existing data.

```
steps:
- name: gcr.io/$PROJECT_ID/unzip
  args: ['-o', '-q', 'cache.zip']
```
