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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.0.0/STCore.xcframework.zip",
            checksum: "31724a63ae6f688f1a8cea5a9801c7786106b4246bc07659d6548af02494bc0f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.0.0/STDatabase.xcframework.zip",
            checksum: "2419d77a397a0fb7e57f676f82c38b5801b6a7b70e36371ed9b8fccae0fec6e4"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.0.0/STNetwork.xcframework.zip",
            checksum: "faedb6372477c959d6726f7f6bc9856703dfbd0434cfc1d1515c37c49c434810"
        ),
    ]
)
