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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STCore.xcframework.zip",
            checksum: "033c57b8214da61bfd06e5c8bcc0cc3263228c06fb7620cd8a01e962b2de3d78"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STDatabase.xcframework.zip",
            checksum: "b27bb618335a05268a6827a323cef91e695686297a54ef0f13a26028d9df78d4"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STNetwork.xcframework.zip",
            checksum: "1f97d9ec4b5a1d41a7c3de443bdb0bd7e7ae7b556dc8ba124732612d845423ae"
        ),
    ]
)
