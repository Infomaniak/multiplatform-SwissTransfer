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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.4.0/STCore.xcframework.zip",
            checksum: "c5300183ecc374dec2b94bf5ef223132a03d42b42e19222eee652407d89a9b08"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.4.0/STDatabase.xcframework.zip",
            checksum: "4627a909c03c3e27dabd7fd2c87171df59c08d3db6e21252d280f87dcb73832d"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.4.0/STNetwork.xcframework.zip",
            checksum: "fa78007496bc137434154ba0c688845eaa9d7dad7b50d55db08979a7fc9ac158"
        ),
    ]
)
