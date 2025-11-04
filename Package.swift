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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.5/STCore.xcframework.zip",
            checksum: "71feb747a582bb99a908148e393f045b02f4704b6ff19e0744fcaf96230d1a6d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.5/STDatabase.xcframework.zip",
            checksum: "b9363e9245674f32a6a8615c59cf39480aa32fc354ec8ed5400ad7ac0f21f3ca"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.5/STNetwork.xcframework.zip",
            checksum: "677558c488c116e6d5a74e4a8bd6463394844bb103538f53d211741ea12b795c"
        ),
    ]
)
