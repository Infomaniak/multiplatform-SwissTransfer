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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.7/STCore.xcframework.zip",
            checksum: "3eeeb78318236fab37bb03239667072d4ca862f8934181dbcda6cf313f28d340"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.7/STDatabase.xcframework.zip",
            checksum: "1e2ab4245fae6e8221f491a59f82c7772b6d77483fa12a132a959af05a375604"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.7/STNetwork.xcframework.zip",
            checksum: "b606e3b52566fba65e11e79a8cc3514ac3b19b8806ef37f4e0a7203c4ee9222e"
        ),
    ]
)
