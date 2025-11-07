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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.6/STCore.xcframework.zip",
            checksum: "c7efab83d0442804cf964cffe6ab1db90528e2128e5262cc56dfead187fafdcc"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.6/STDatabase.xcframework.zip",
            checksum: "4ebfb94431e830322e6bf512804c78e3758ef3a4fb396448873d44df3a96a5a5"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.3.6/STNetwork.xcframework.zip",
            checksum: "3de3c3b115aa799d975decad898d5f4fbf47e25aa24569574d60f9939fc59db1"
        ),
    ]
)
