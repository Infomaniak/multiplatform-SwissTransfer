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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.9/STCore.xcframework.zip",
            checksum: "731c5715e60743ae89ccff2948e31b78bb3fce4d0e36781b4ed0b816288c4313"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.9/STDatabase.xcframework.zip",
            checksum: "1c328dcbf28f5d838dcea8fce73bea28c83d0801b09346f56999f97060db8082"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.9/STNetwork.xcframework.zip",
            checksum: "4724bae1f5b5c1cf3322b4deb037cbcf3cf46df20142eec3203d6b56c8e3b497"
        ),
    ]
)
