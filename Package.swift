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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.5.0/STCore.xcframework.zip",
            checksum: "14c12952f27a4419e4d8d0ed19762bf099673686c98bdf3892485fb8b2ca01e5"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.5.0/STDatabase.xcframework.zip",
            checksum: "0d5b13011ca0f8ba9902a011096749c39b0bbfa9e980e2906ae057d5ed2c659f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.5.0/STNetwork.xcframework.zip",
            checksum: "0ca7cdcf05849ab1c5bfa7b0596fd1357ad56b2625e233e9d5dfc250660b9965"
        ),
    ]
)
