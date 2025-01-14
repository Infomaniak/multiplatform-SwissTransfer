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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.0/STCore.xcframework.zip",
            checksum: "3ba493a82fa50b056b584d5cdf2e47159150411adb0cc219bde9119993b88640"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.0/STDatabase.xcframework.zip",
            checksum: "f9075a0cf8d3435baae2cfc1ae37a7ba189e263a4659d2208bd7a867d4da853a"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.11.0/STNetwork.xcframework.zip",
            checksum: "13f24ffd83e70e0d13841a0fb7168eea8b5d2518745908b64f59b29db8fa87fd"
        ),
    ]
)
