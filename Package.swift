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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.1/STCore.xcframework.zip",
            checksum: "8a15fbfe6be94539f8e931e7c6e9b31044e40af06fae1bbd8aaf39e5c240cdaa"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.1/STDatabase.xcframework.zip",
            checksum: "f0cd2a1b67baae80574bcfb8747e727a03f011ad9b689c126e0ae6e9d128d42e"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.7.1/STNetwork.xcframework.zip",
            checksum: "91a897354991fdab516b146c49d719506690c2b4ec0b477a9a597355f95fcd73"
        ),
    ]
)
