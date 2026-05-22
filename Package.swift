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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.0/STCore.xcframework.zip",
            checksum: "ba9c8b7dd75fa2f965b7aeaa5bdadd09cc7e2fd3a63a59a709b269a2da73e8bf"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.0/STDatabase.xcframework.zip",
            checksum: "ea7b3aec7a12d3ab90d463884d70fdbb3aa905d640b68541b8eebf7596277257"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/10.1.0/STNetwork.xcframework.zip",
            checksum: "855cf4a9bdfc6eff4de41476ac3ac973d0da4a2b96da8cc3ab9f3c4a9e8ebc94"
        ),
    ]
)
