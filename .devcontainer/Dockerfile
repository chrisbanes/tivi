# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.137.0/containers/java/.devcontainer/base.Dockerfile
ARG JDK_VARIANT="11"
FROM mcr.microsoft.com/vscode/devcontainers/java:0-${JDK_VARIANT}

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# Download and install Android SDK
ARG ANDROID_SDK_VERSION=6609375

ENV ANDROID_SDK_ROOT /opt/android-sdk
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && cd ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_VERSION}_latest.zip && \
    unzip *commandlinetools*linux*.zip && \
    rm *commandlinetools*linux*.zip

# Install the basic SDK components
RUN yes Y | ${ANDROID_SDK_ROOT}/cmdline-tools/tools/bin/sdkmanager --install \
    "platform-tools" \
    "build-tools;30.0.2" \
    "platforms;android-30"
    # TODO: Investigate installing the emulator
    #"system-images;android-*30*;google_apis;x86" \
    #"emulator"

# Accept the SDK licenses
RUN yes Y | ${ANDROID_SDK_ROOT}/cmdline-tools/tools/bin/sdkmanager --licenses

# Add the tools and platform-tools to PATH
ENV PATH="${PATH}:${ANDROID_SDK_ROOT}/tools:${ANDROID_SDK_ROOT}/platform-tools"
