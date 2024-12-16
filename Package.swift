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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.7/STCore.xcframework.zip",
            checksum: "3ee67df0bc4ccb97639f5b752576b339bf2bc279c9cfc703e30154755f3d8ce9"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.7/STDatabase.xcframework.zip",
            checksum: "bc11e6c31c295677e98a0e2101bbf1f5db5a54fcb16160169a5fa586d65ddf20"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.9.7/STNetwork.xcframework.zip",
            checksum: "994cf949e561053743ff339a598d7fee3aeba35f37b87a41a913124d717991c6"
        ),
    ]
)
