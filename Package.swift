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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.1/STCore.xcframework.zip",
            checksum: "4315d7ac354d2700e55849e0d92d9a24c763d16282d366ec8401702a108e9a0f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.1/STDatabase.xcframework.zip",
            checksum: "0c8fa6f70adda24ee49a41c366ad726bb2d1ce31a3fc79ed2505cbad9221cfb8"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.1/STNetwork.xcframework.zip",
            checksum: "10b3106b11507696ce4b43cdafcae17228cadbf5fdb7ab1f2c4bce00f32c3353"
        ),
    ]
)
