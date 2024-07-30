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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STCore.xcframework.zip",
            checksum: "4dffa7bf322805ed53bd450444d3abb7b3cfee40cfe0c5a70343206bbb204fc1"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STDatabase.xcframework.zip",
            checksum: "b535793a27c240b6b352c8989d257ac9e4647fbafafc48c96ef440bf05c047a8"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.0.2/STNetwork.xcframework.zip",
            checksum: "cc2f2e1cac12b41ab93228b36c54d42215bbf35bc2e1e45afbe514402f2c7de8"
        ),
    ]
)
