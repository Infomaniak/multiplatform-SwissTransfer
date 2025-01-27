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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.3/STCore.xcframework.zip",
            checksum: "78ce5a92add2e7d082df264ec7b91e00d2c55a015236da75fd905a3ef52282d4"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.3/STDatabase.xcframework.zip",
            checksum: "cb2da971ef877ffa85d87114b2d1a786cbba7b345581220c31fa4601f1943425"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.3/STNetwork.xcframework.zip",
            checksum: "c94996adbb1d66175d8a8403f3b001a8cfaebe1419452c113a4a88b3956f1fef"
        ),
    ]
)
