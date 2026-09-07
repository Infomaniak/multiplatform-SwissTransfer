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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.0/STCore.xcframework.zip",
            checksum: "87177354246a40a802a4ba23a773d3c1de2e208ef69f80163b2ffd172796fb04"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.0/STDatabase.xcframework.zip",
            checksum: "ccd4a9517384bc1e19ef2eaf316f8c410c6012476d4f47865db0da1a384ef84e"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.0/STNetwork.xcframework.zip",
            checksum: "0a78107fac82da62cfc5893f08332ca559ac7af3c8365085ef9475328128532e"
        ),
    ]
)
