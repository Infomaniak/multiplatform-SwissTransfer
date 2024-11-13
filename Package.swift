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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.1/STCore.xcframework.zip",
            checksum: "2c47bc981159d0fb091c7d9b0d3fcfa26cb54338cf6af2c208dd8fce7aa3de11"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.1/STDatabase.xcframework.zip",
            checksum: "19392c0c6251dc3c9cb8abd60cbd521d7a999be0c83dd74dca687a8abc725ac5"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.8.1/STNetwork.xcframework.zip",
            checksum: "5d5f822de73b42b817639b98f87f2520b6dac26aee815c706fbdbe2523aac1ae"
        ),
    ]
)
