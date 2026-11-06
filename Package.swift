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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-13.0.0-202611060812-42184782782-2/STCore.xcframework.zip",
            checksum: "b27bdd0550e16b57918ea17dc9ff01fd5dcfc3eb6bb4f076176d337c24a240f8"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-13.0.0-202611060812-42184782782-2/STDatabase.xcframework.zip",
            checksum: "a9d28e997c3feb18bc49249323f8cf04abe0a7ad0e6c09073cdbdfc2ec9e05ea"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-13.0.0-202611060812-42184782782-2/STNetwork.xcframework.zip",
            checksum: "291ecc3289ce0bb88c1907675675339a50947e6c614f8f956dc5ebc18327b37e"
        ),
    ]
)
