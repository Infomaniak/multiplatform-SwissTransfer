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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.3/STCore.xcframework.zip",
            checksum: "e5325b842c528b9437a1bdfade6fc0d690451639fe63c5ef319a5cf88de61ba5"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.3/STDatabase.xcframework.zip",
            checksum: "90fc78686cf5d9514ee93df22fe6d8c0816bab71461bb2bfe2155717fc404edf"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/0.10.3/STNetwork.xcframework.zip",
            checksum: "8bd2a03da51e16a2687ae0ed0c6ac0000327ace54a4c74e25c7d600f921c5727"
        ),
    ]
)
