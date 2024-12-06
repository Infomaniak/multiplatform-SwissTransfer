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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.5/STCore.xcframework.zip",
            checksum: "a261b65f74a9f8f1fedcca8ed59882d0b320c5734f3df6970924c7e623535a0d"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.5/STDatabase.xcframework.zip",
            checksum: "d20c0ea0c8c55cb95ae300a1c8ba1f94450154fffaa2ee92ffa6b3c8355deb3e"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.5/STNetwork.xcframework.zip",
            checksum: "260ecf7562216fbe15c5e1c18141fb3cc5083af31ac3569a6da893fd8fa12f37"
        ),
    ]
)
