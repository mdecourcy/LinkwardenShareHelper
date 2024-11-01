# Linkwarden Share Helper

A simple Android app that allows you to share links directly to your Linkwarden instance.

## Disclaimer

This is a barebones app created by someone who is not an Android developer. It provides basic functionality with minimal error handling and UI polish. Use at your own risk. While it does implement secure storage of credentials, there may be bugs or security issues I'm not aware of.

This app is not officially associated with Linkwarden and is provided as-is without any guarantees.

## Features

- Share links from any app to Linkwarden
- Securely store server details and API token using encryption
- OLED black theme
- Minimal interface

## Setup

1. Install the app
2. Open the app and enter your Linkwarden server details:
   - Server URL (e.g., `https://linkwarden.example.com`)
   - API Token (from your Linkwarden settings)
3. Click "Save Settings"

## Usage

1. In any app, find a link you want to save
2. Share the link
3. Select "Linkwarden Share Helper" from the share menu
4. The link will be saved to your Linkwarden instance

## Security

- API token is stored using Android's EncryptedSharedPreferences
- Uses AES256 encryption for stored credentials
- No data is sent to any third parties

## Building

1. Clone the repository
2. Open in Android Studio
3. Build using Gradle:

## Requirements

- Android 6.0 (API level 23) or higher
- Linkwarden instance with API access

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat/Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Acknowledgments

- [Linkwarden](https://github.com/linkwarden/linkwarden) for the amazing bookmark manager