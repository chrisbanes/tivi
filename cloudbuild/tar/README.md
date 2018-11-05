# tar

This is a tool build to simply invoke the
[`tar`](https://linux.die.net/man/1/tar) command.

Arguments passed to this builder will be passed to `tar` directly.

## Examples

The following examples demonstrate build requests that use this builder.

### Archive and compress caches

This `cloudbuild.yaml` archives and compresses the build cache directories.

```
steps:
- name: gcr.io/$PROJECT_ID/tar
  args: ['cpzf', 'cache.tar.gz', '.gradle', '/root/.gradle', '/root/.m2']
```

### Extract an existing archive

```
steps:
- name: gcr.io/$PROJECT_ID/tar
  args: ['cpzf', 'cache.tar.gz', '.gradle', '/root/.gradle', '/root/.m2']
```