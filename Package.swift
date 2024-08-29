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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.0/STCore.xcframework.zip",
            checksum: "68796aee21b8788ca23a1e4407ea0b1891303d4c75f7d9b16a860b31a192eec5"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.0/STDatabase.xcframework.zip",
            checksum: "4a074ab59ba985034cf2eb4db775a749c8d3e2495bbf466a9ea327237826a24f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.1.0/STNetwork.xcframework.zip",
            checksum: "cffe8d3ea787db6ff39ab3a7e24d16da9f0a415e78f6ab8a3c2dbbfc8b4baf14"
        ),
    ]
)
