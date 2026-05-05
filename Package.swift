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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.0/STCore.xcframework.zip",
            checksum: "67f0d16ac437eb0d6ab82fffbd011bf22f5c2f3b1d21795ece015f2dcda6c2c8"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.0/STDatabase.xcframework.zip",
            checksum: "5e22b32c60e6a0a0dde67dcb5b73438a316e10aae7b34cbf2940cf779957caa5"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.0.0/STNetwork.xcframework.zip",
            checksum: "98224026b06050253b5d4cce207f6a5c3b8cadf1ff308afe1e65e7083eef9ffd"
        ),
    ]
)
