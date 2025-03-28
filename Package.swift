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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.3/STCore.xcframework.zip",
            checksum: "4f4151baa37a73c6226293117c40df941ea961998909b4f3078489107c9c0d8f"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.3/STDatabase.xcframework.zip",
            checksum: "04ac4025afe9cfbd5833e1480697e31bf2068eaab3d7e9e8f365f244ca1e0385"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.3/STNetwork.xcframework.zip",
            checksum: "4c43bd2a6ac905488554873b7996e249641fb94cac4b6a9834a606b9a11912d6"
        ),
    ]
)
