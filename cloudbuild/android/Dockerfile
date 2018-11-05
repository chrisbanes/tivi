# we use a gcr.io image and not openjdk:8-jdk-slim because it loads faster in the google Google Cloud Build environment
FROM gcr.io/cloud-builders/javac

LABEL maintainer="ryan@pixiteapps.com"

ENV DEBIAN_FRONTEND=noninteractive

# make Apt non-interactive
RUN echo 'APT::Get::Assume-Yes "true";' > /etc/apt/apt.conf.d/90builder \
  && echo 'DPkg::Options "--force-confnew";' >> /etc/apt/apt.conf.d/90builder

# Install Dependencies
RUN apt-get update && \
    apt-get install -y \
        git locales sudo openssh-client ca-certificates tar gzip parallel \
        zip unzip bzip2 gnupg curl wget

# Set timezone to UTC by default
RUN ln -sf /usr/share/zoneinfo/Etc/UTC /etc/localtime

# Use unicode
RUN locale-gen C.UTF-8 || true
ENV LANG=C.UTF-8

ARG sdk_version=sdk-tools-linux-4333796.zip
ARG android_home=/opt/android/sdk

# Install Android SDK
RUN sudo mkdir -p ${android_home} && \
    curl --silent --show-error --location --fail --retry 3 --output /tmp/${sdk_version} https://dl.google.com/android/repository/${sdk_version} && \
    unzip -q /tmp/${sdk_version} -d ${android_home} && \
    rm /tmp/${sdk_version}

# Set environment variables
ENV ANDROID_HOME ${android_home}
ENV ADB_INSTALL_TIMEOUT 120
ENV PATH=${ANDROID_HOME}/emulator:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}

RUN mkdir ~/.android && echo '### User Sources for Android SDK Manager' > ~/.android/repositories.cfg

RUN yes | sdkmanager --licenses && sdkmanager --update

# Update SDK manager and install system image, platform and build tools
RUN sdkmanager \
    "tools" \
    "platform-tools" \
    "build-tools;28.0.3" \
    "platforms;android-28"
