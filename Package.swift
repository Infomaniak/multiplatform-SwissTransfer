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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.6/STCore.xcframework.zip",
            checksum: "51f4ae89489d3c6f4bbd726a20da546a160542f129e0505cdf40b7158efcfa92"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.6/STDatabase.xcframework.zip",
            checksum: "53aafbf17634000f3a659a802a911de30e3b8ef1b4dde6b44e956efa0ae0820f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.6/STNetwork.xcframework.zip",
            checksum: "65f64701154b6c6252d619b31dd024eea2c4804fcaa826f755258243b3da5261"
        ),
    ]
)
