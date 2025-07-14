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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.1/STCore.xcframework.zip",
            checksum: "e77ab75f6e74df165c1c3e79b84773f8e4400c07e5bb62ce0123c47223af8c25"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.1/STDatabase.xcframework.zip",
            checksum: "4dedfb37c87aa6589b8f3c961a9436f6e509ea34080b3f67a65a57a900dd53a5"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.1/STNetwork.xcframework.zip",
            checksum: "3da6a6f602ec94a2eec916ffadc082049da75265ae1b21e06904c2ecdb725831"
        ),
    ]
)
