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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.1/STCore.xcframework.zip",
            checksum: "cad84d2658de46f97c4cf251171e4bcc6205ac7277c1eb9b7d21b208b99a5dc4"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.1/STDatabase.xcframework.zip",
            checksum: "5d93451767d7f78e4838d2619aef00351d63df5a787d85beafc82e92da3a09b7"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.1.1/STNetwork.xcframework.zip",
            checksum: "a7b3aa363dbf0c58ee81a55b462a01a64a3076901d1185334258569e8bd5ea3d"
        ),
    ]
)
