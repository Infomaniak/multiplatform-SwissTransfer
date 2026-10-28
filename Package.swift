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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.2/STCore.xcframework.zip",
            checksum: "dcf2d4f1cbe86457638023347e850d5027bb57c61557196b544fcd25e6572e8b"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.2/STDatabase.xcframework.zip",
            checksum: "579da9fd088699b653293a7873a5f77577bee1ef55542acc669c11bacf291b7f"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.1.2/STNetwork.xcframework.zip",
            checksum: "183da4548a731d6384beb64367b5e59cfb86dadf3f6ad3ff0c2f640352699190"
        ),
    ]
)
