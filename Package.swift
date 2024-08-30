// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.1/STCore.xcframework.zip",
            checksum: "dd86fbfafb62238dca9a5058d8be8097e0ce7cebf76fd38d8ca4d35f5b023141"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.1/STDatabase.xcframework.zip",
            checksum: "50aacab839a923945c8cd6af578eb01c3cc524caebd2556a2c5e5bc79e0370bf"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.1/STNetwork.xcframework.zip",
            checksum: "2fb930757a9211d7889fffbdef34fbcf29b91228898b9b9aa357ee4926193553"
        ),
    ]
)
